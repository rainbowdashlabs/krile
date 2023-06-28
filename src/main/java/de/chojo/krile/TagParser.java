package de.chojo.krile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TagParser {
    private final ObjectMapper mapper = YAMLMapper.builder()
            .build();
    private final TagRepository tagRepository;
    private final Path filePath;

    public TagParser(TagRepository tagRepository, Path filePath) {
        this.tagRepository = tagRepository;
        this.filePath = filePath;
    }

    public static TagParser parse(TagRepository tagRepository, Path path) {
        return new TagParser(tagRepository, path);
    }

    private List<Author> getAuthors(Git git, Path path) throws GitAPIException {
        List<Author> authors = new ArrayList<>();
        BlameResult blameResult = new Git(git.getRepository()).blame().setFilePath(path.toString()).call();

        for (int i = 0; i < blameResult.getResultContents().size(); i++) {
            RevCommit commit = blameResult.getSourceCommit(i);
            PersonIdent author = commit.getAuthorIdent();
            authors.add(new Author(author.getName(), author.getEmailAddress()));
        }
        return authors;
    }

    private FileMeta meta() throws IOException, GitAPIException {
        return new FileMeta(getTimeCreate(), getTimeModified());
    }

    private Optional<FileEvent> getTimeCreate() throws GitAPIException {
        Iterable<RevCommit> commits = new Git(repository()).log().addPath(filePath.toString()).call();
        RevCommit firstcommit = null;
        for (RevCommit commit : commits) {
            firstcommit = commit;
            break;
        }
        if(firstcommit == null) return Optional.empty();
        return Optional.of(new FileEvent(firstcommit.getCommitterIdent().getWhenAsInstant(), Author.of(firstcommit.getAuthorIdent())));
    }

    private Optional<FileEvent> getTimeModified() throws GitAPIException {
        Iterable<RevCommit> commits = new Git(repository()).log().addPath(filePath.toString()).call();

        RevCommit lastCommit = null;
        for (RevCommit commit : commits) {
            lastCommit = commit;
        }
        if(lastCommit == null) return Optional.empty();
        return Optional.of(new FileEvent(lastCommit.getCommitterIdent().getWhenAsInstant(), Author.of(lastCommit.getAuthorIdent())));
    }

    private Repository repository() {
        return tagRepository.git().getRepository();
    }

    public Tag tag() {
        return null;
    }
}
