package de.chojo.krile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagParser {
    private static final ObjectMapper MAPPER = YAMLMapper.builder()
            .build();
    private final TagRepository tagRepository;
    private final Path filePath;
    @RegExp
    public static final String TAG = "^---$\n(?<meta>.+?)^---$\n(?<tag>.+)";
    private static final Pattern TAG_PATTERN = Pattern.compile(TAG, Pattern.DOTALL);

    public TagParser(TagRepository tagRepository, Path filePath) {
        this.tagRepository = tagRepository;
        this.filePath = filePath;
    }

    public static TagParser parse(TagRepository tagRepository, Path path) {
        return new TagParser(tagRepository, path);
    }

    public List<Author> getAuthors(Git git, Path path) throws GitAPIException {
        List<Author> authors = new ArrayList<>();
        BlameResult blameResult = new Git(git.getRepository()).blame().setFilePath(path.toString()).call();

        for (int i = 0; i < blameResult.getResultContents().size(); i++) {
            RevCommit commit = blameResult.getSourceCommit(i);
            PersonIdent author = commit.getAuthorIdent();
            authors.add(new Author(author.getName(), author.getEmailAddress()));
        }
        return authors;
    }

    public FileMeta fileMeta() throws GitAPIException {
        return new FileMeta(filePath.toFile().getName(), getTimeCreate(), getTimeModified());
    }

    public String tagContent() throws IOException {
        String fileContent = getFileContent();
        Matcher matcher = TAG_PATTERN.matcher(fileContent);
        if (matcher.find()) return matcher.group("tag");
        return fileContent;
    }

    public TagMeta tagMeta() throws IOException {
        String tagText = Files.readString(filePath);
        String id = filePath.toFile().getName().replace(".md", "");
        if (tagText.startsWith("---")) {
            return MAPPER.readValue(tagText, TagMeta.class)
                    .inject(id, id);
        }
        return TagMeta.createDefault(id);
    }

    private Optional<FileEvent> getTimeCreate() throws GitAPIException {
        Iterable<RevCommit> commits = new Git(repository()).log().addPath(filePath.toString()).call();
        RevCommit firstcommit = null;
        for (RevCommit commit : commits) {
            firstcommit = commit;
            break;
        }
        if (firstcommit == null) return Optional.empty();
        return Optional.of(new FileEvent(firstcommit.getCommitterIdent().getWhenAsInstant(), Author.of(firstcommit.getAuthorIdent())));
    }

    private Optional<FileEvent> getTimeModified() throws GitAPIException {
        Iterable<RevCommit> commits = new Git(repository()).log().addPath(filePath.toString()).call();

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

    public Tag tag() throws IOException, GitAPIException {
        return new Tag(tagMeta(), fileMeta(), tagContent());
    }

    private String getFileContent() throws IOException {
        return Files.readString(filePath);
    }
}
