package de.chojo.krile.tagimport.tag.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.chojo.krile.tagimport.repo.TagRepository;
import de.chojo.krile.tagimport.tag.RawTag;
import de.chojo.krile.tagimport.tag.entities.Author;
import de.chojo.krile.tagimport.tag.entities.FileEvent;
import de.chojo.krile.tagimport.tag.entities.FileMeta;
import de.chojo.krile.tagimport.tag.entities.TagMeta;
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
    private static final ObjectMapper MAPPER = YAMLMapper.builder()
            .build();
    private final TagRepository tagRepository;
    private final Path filePath;
    @RegExp
    public static final String TAG = "^---$\\n(?<meta>.+?)^---$\\n(?<tag>.+)";
    private static final Pattern TAG_PATTERN = Pattern.compile(TAG, Pattern.DOTALL);

    public TagParser(TagRepository tagRepository, Path filePath) {
        this.tagRepository = tagRepository;
        this.filePath = filePath;
    }

    public static TagParser parse(TagRepository tagRepository, Path path) {
        return new TagParser(tagRepository, path);
    }

    public Collection<Author> getAuthors() throws GitAPIException {
        Set<Author> authors = new HashSet<>();
        BlameResult blameResult = new Git(repository()).blame().setFilePath(relativePath().toString()).call();

        for (int i = 0; i < blameResult.getResultContents().size(); i++) {
            RevCommit commit = blameResult.getSourceCommit(i);
            PersonIdent author = commit.getAuthorIdent();
            authors.add(new Author(author.getName(), author.getEmailAddress()));
        }
        return authors;
    }

    public FileMeta fileMeta() throws GitAPIException {
        return new FileMeta(filePath.toFile().getName(), getAuthors(), getTimeCreate(), getTimeModified());
    }

    public String tagContent() throws IOException {
        return tagFile().content();
    }

    public TagMeta tagMeta() throws IOException {
        TagFile file = tagFile();
        String id = filePath.toFile().getName().replace(".md", "");
        if (file.meta().isPresent()) {
            return MAPPER.readValue(file.meta().get(), TagMeta.class)
                    .inject(id, id);
        }
        return TagMeta.createDefault(id);
    }

    private TagFile tagFile() throws IOException {
        String fileContent = getFileContent();
        if (fileContent.startsWith("---")) {
            var split = Pattern.compile("^---$", Pattern.MULTILINE).split(fileContent);
            return new TagFile(Optional.of(split[1]), split[2].trim());
        }
        return new TagFile(Optional.empty(), fileContent);
    }

    private Optional<FileEvent> getTimeCreate() throws GitAPIException {
        Iterable<RevCommit> commits = new Git(repository()).log().addPath(relativePath().toString()).call();
        RevCommit firstcommit = null;
        for (RevCommit commit : commits) {
            firstcommit = commit;
            break;
        }
        if (firstcommit == null) return Optional.empty();
        return Optional.of(new FileEvent(firstcommit.getCommitterIdent().getWhenAsInstant(), Author.of(firstcommit.getAuthorIdent())));
    }

    private Optional<FileEvent> getTimeModified() throws GitAPIException {
        Iterable<RevCommit> commits = new Git(repository()).log().addPath(relativePath().toString()).call();

        RevCommit lastCommit = null;
        for (RevCommit commit : commits) {
            lastCommit = commit;
        }
        if (lastCommit == null) return Optional.empty();
        return Optional.of(new FileEvent(lastCommit.getCommitterIdent().getWhenAsInstant(), Author.of(lastCommit.getAuthorIdent())));
    }

    private Repository repository() {
        return tagRepository.git().getRepository();
    }

    public RawTag tag() throws IOException, GitAPIException {
        return new RawTag(tagMeta(), fileMeta(), tagContent());
    }

    private String getFileContent() throws IOException {
        return Files.readString(filePath);
    }

    private Path relativePath() {
        return tagRepository.relativize(filePath);
    }
}
