package de.chojo.krile;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TagRepository implements Closeable {
    private final Path path;
    private final Git git;

    public TagRepository(Path path, Git git) {
        this.path = path;
        this.git = git;
    }

    public static TagRepository create(String url) throws IOException, GitAPIException {
        Path git = Files.createTempDirectory("git");
        Git.cloneRepository()
                .setURI(url)
                .setDirectory(git.toFile())
                .call();
        return create(git);
    }

    public static TagRepository create(Path git) throws IOException {
        return new TagRepository(git, Git.open(git.toFile()));
    }

    public List<Tag> tags() {
        try (var files = Files.list(path)) {
            List<Path> list = files.filter(p -> p.toFile().isFile())
                    .filter(p -> p.toFile().getName().endsWith(".md"))
                    .toList();
            List<Tag> tags = new ArrayList<>();
            for (Path tagPath : list) {
                TagParser parse = TagParser.parse(this, path);
                tags.add(parse.tag());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Collections.emptyList();
    }

    @Override
    public void close() throws IOException {
        git.close();
    }

    public Git git() {
        return git;
    }
}
