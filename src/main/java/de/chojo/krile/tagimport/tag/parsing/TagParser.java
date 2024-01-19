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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class TagParser {
    public static final ObjectMapper MAPPER = YAMLMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .build();
    private final RawRepository tagRepository;
    private final Path filePath;

    /**
     * Initializes a new instance of the TagParser class.
     *
     * @param tagRepository The RawRepository to use for tag information.
     * @param filePath      The path of the file to parse.
     */
    private TagParser(RawRepository tagRepository, Path filePath) {
        this.tagRepository = tagRepository;
        this.filePath = filePath;
    }

    /**
     * Parses the given file using the RawRepository for tag information.
     *
     * @param tagRepository The RawRepository to use for tag information.
     * @param path          The path of the file to parse.
     * @return A new instance of the TagParser class.
     */
    public static TagParser parse(RawRepository tagRepository, Path path) {
        return new TagParser(tagRepository, path);
    }

    /**
     * Retrieves the authors of the file.
     *
     * @return A collection of RawAuthor objects representing the authors of the file.
     * @throws ImportException If an error occurs while retrieving the authors.
     */
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

    /**
     * Retrieves the FileMeta object containing metadata about the file.
     *
     * @return The FileMeta object containing metadata about the file.
     * @throws ImportException If an error occurs while retrieving the file metadata.
     */
    public FileMeta fileMeta() throws ImportException {
        return new FileMeta(filePath.toFile().getName(), getAuthors(), getTimeCreated(), getTimeModified());
    }

    /**
     * Retrieves the content of the file's tag.
     *
     * @return The content of the file's tag.
     * @throws ImportException If an error occurs while retrieving the tag content.
     */
    public String tagContent() throws ImportException {
        return tagFile().content();
    }

    /**
     * Retrieves the metadata of the file's tag.
     *
     * @return The metadata of the file's tag.
     * @throws ParsingException If an error occurs while parsing the metadata.
     * @throws ImportException  If an error occurs while retrieving the tag metadata.
     */
    public RawTagMeta tagMeta() throws ParsingException, ImportException {
        TagFile file = tagFile();
        String id = tagRepository.tagPath().relativize(filePath).toString().replace(".md", "");
        String tag = tagRepository.tagPath().relativize(filePath).getFileName().toString().replace(".md", "");
        return RawTagMeta.parse(file, id, tag);
    }

    /**
     * Retrieves the complete information of the file's tag.
     *
     * @return A RawTag object containing the tag metadata, file metadata, and tag content.
     * @throws ParsingException If an error occurs while parsing the tag metadata or tag content.
     * @throws ImportException  If an error occurs while retrieving the tag metadata or file metadata.
     */
    public RawTag tag() throws ParsingException, ImportException {
        return new RawTag(tagMeta(), fileMeta(), tagContent());
    }

    /**
     * Retrieves the information of the file's tag.
     *
     * @return A TagFile object containing the file content and optional tag metadata and tag content.
     * @throws ImportException If an error occurs while retrieving the file content.
     */
    private TagFile tagFile() throws ImportException {
        return parseTagFile(getFileContent());
    }

    public static TagFile parseTagFile(String fileContent) {
        if (fileContent.startsWith("---")) {
            var split = Pattern.compile("^---$", Pattern.MULTILINE).split(fileContent);
            return new TagFile(Optional.of(split[1]), split[2].trim());
        }
        return new TagFile(Optional.empty(), fileContent);
    }

    /**
     * Retrieves the time when the file was last modified.
     *
     * @return A FileEvent object containing the time of the last modification and the author who made the modification.
     * @throws ImportException If an error occurs while retrieving the commit information.
     */
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

    /**
     * Retrieves the time when the file was created.
     *
     * @return A FileEvent object containing the time of creation and the author who made the initial commit.
     * @throws ImportException If an error occurs while retrieving the commit information.
     */
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

    /**
     * Retrieves the content of the file.
     *
     * @return The content of the file as a string.
     * @throws ImportException If an error occurs while reading the file.
     */
    private String getFileContent() throws ImportException {
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            throw new ImportException("Could not read file", e);
        }
    }

    /**
     * Retrieves the relative path of the file.
     *
     * @return The relative path of the file as a Path object.
     */
    private Path relativePath() {
        return tagRepository.relativize(filePath);
    }
}
