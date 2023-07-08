/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.tagimport.tag.parsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.chojo.krile.tagimport.exception.ImportException;
import de.chojo.krile.tagimport.exception.ParsingException;
import de.chojo.krile.tagimport.repo.RawRepository;
import de.chojo.krile.tagimport.tag.RawTag;
import de.chojo.krile.tagimport.tag.entities.FileEvent;
import de.chojo.krile.tagimport.tag.entities.FileMeta;
import de.chojo.krile.tagimport.tag.entities.RawAuthor;
import de.chojo.krile.tagimport.tag.entities.RawTagMeta;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.intellij.lang.annotations.RegExp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class TagParser {
    @RegExp
    public static final String TAG = "^---$\\n(?<meta>.+?)^---$\\n(?<tag>.+)";
    private static final ObjectMapper MAPPER = YAMLMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .build();
    private static final Pattern TAG_PATTERN = Pattern.compile(TAG, Pattern.DOTALL);
    private final RawRepository tagRepository;
    private final Path filePath;

    public TagParser(RawRepository tagRepository, Path filePath) {
        this.tagRepository = tagRepository;
        this.filePath = filePath;
    }

    public static TagParser parse(RawRepository tagRepository, Path path) {
        return new TagParser(tagRepository, path);
    }

    public Collection<RawAuthor> getAuthors() throws ImportException {
        Set<RawAuthor> rawAuthors = new HashSet<>();
        BlameResult blameResult = null;
        try {
            blameResult = new Git(repository()).blame().setFilePath(relativePath().toString()).call();
        } catch (GitAPIException e) {
            throw new ImportException("Could not create blame", e);
        }

        for (int i = 0; i < blameResult.getResultContents().size(); i++) {
            RevCommit commit = blameResult.getSourceCommit(i);
            if (commit == null) continue;
            PersonIdent author = commit.getAuthorIdent();
            rawAuthors.add(new RawAuthor(author.getName(), author.getEmailAddress()));
        }
        return rawAuthors;
    }

    public FileMeta fileMeta() throws ImportException {
        return new FileMeta(filePath.toFile().getName(), getAuthors(), getTimeModified(), getTimeCreated());
    }

    public String tagContent() throws ImportException {
        return tagFile().content();
    }

    public RawTagMeta tagMeta() throws ParsingException, ImportException {
        TagFile file = tagFile();
        String id = filePath.toFile().getName().replace(".md", "");
        if (file.meta().isPresent()) {
            try {
                return MAPPER.readValue(file.meta().get(), RawTagMeta.class)
                        .inject(id, id);
            } catch (JsonProcessingException e) {
                throw new ParsingException("Failed to parse tag meta.%n%s".formatted(e.getMessage()), e);
            }
        }
        return RawTagMeta.createDefault(id);
    }

    public RawTag tag() throws ParsingException, ImportException {
        return new RawTag(tagMeta(), fileMeta(), tagContent());
    }

    private TagFile tagFile() throws ImportException {
        String fileContent = getFileContent();
        if (fileContent.startsWith("---")) {
            var split = Pattern.compile("^---$", Pattern.MULTILINE).split(fileContent);
            return new TagFile(Optional.of(split[1]), split[2].trim());
        }
        return new TagFile(Optional.empty(), fileContent);
    }

    private FileEvent getTimeModified() throws ImportException {
        Iterable<RevCommit> commits;
        try {
            commits = new Git(repository()).log().addPath(relativePath().toString()).call();
        } catch (GitAPIException e) {
            throw new ImportException(e);
        }
        RevCommit firstcommit = null;
        for (RevCommit commit : commits) {
            firstcommit = commit;
            break;
        }
        if (firstcommit == null) throw new RuntimeException("Could not parse time created");
        return new FileEvent(firstcommit.getCommitterIdent().getWhenAsInstant(), RawAuthor.of(firstcommit.getAuthorIdent()));
    }

    private FileEvent getTimeCreated() throws ImportException {
        Iterable<RevCommit> commits;
        try {
            commits = new Git(repository()).log().addPath(relativePath().toString()).call();
        } catch (GitAPIException e) {
            throw new ImportException(e);
        }

        RevCommit lastCommit = null;
        for (RevCommit commit : commits) {
            lastCommit = commit;
        }
        if (lastCommit == null) throw new ImportException("Could not parse time modified");
        return new FileEvent(lastCommit.getCommitterIdent().getWhenAsInstant(), RawAuthor.of(lastCommit.getAuthorIdent()));
    }

    private Repository repository() {
        return tagRepository.git().getRepository();
    }

    private String getFileContent() throws ImportException {
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            throw new ImportException("Could not read file", e);
        }
    }

    private Path relativePath() {
        return tagRepository.relativize(filePath);
    }
}
