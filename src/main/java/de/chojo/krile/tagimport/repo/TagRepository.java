package de.chojo.krile.tagimport.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.chojo.krile.tagimport.tag.RawTag;
import de.chojo.krile.tagimport.tag.parsing.TagParser;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class TagRepository implements Closeable {
    private static final ObjectMapper MAPPER = YAMLMapper.builder()
            .build();
    private static final Logger log = getLogger(TagRepository.class);
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

    public RepoConfig configuration() {
        // TODO: Lazy loading and cache
        Optional<Path> path = findConfigPath();
        if (path.isPresent()) {
            try {
                return MAPPER.readValue(path.get().toFile(), RepoConfig.class);
            } catch (IOException e) {
                // ignore
            }
        }
        return RepoConfig.DEFAULT;
    }

    private Optional<Path> findConfigPath() {
        // TODO: Probably a bot config value
        final String[] paths = new String[]{".krile", ".github", ".gitlab", ""};
        final String[] files = new String[]{"krile.yaml", "krile.yml"};
        for (String currPath : paths) {
            for (String currFile : files) {
                Path resolved = path.resolve(currPath).resolve(currFile);
                if (resolved.toFile().exists()) return Optional.of(resolved);
            }
        }
        return Optional.empty();
    }

    public List<RawTag> tags() throws IOException {
        try (var files = Files.list(tagPath())) {
            List<Path> list = files.filter(p -> p.toFile().isFile())
                    .filter(p -> p.toFile().getName().endsWith(".md"))
                    .filter(p -> configuration().included(p))
                    .toList();
            List<RawTag> tags = new ArrayList<>();
            for (Path tagPath : list) {
                TagParser parse = TagParser.parse(this, tagPath);
                try {
                    tags.add(parse.tag());
                } catch (GitAPIException e) {
                    log.error("Could not load tag", e);
                }
            }
            return tags;
        }
    }

    @Override
    public void close() throws IOException {
        git.close();
        try (var stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    public Git git() {
        return git;
    }

    public Path path() {
        return path;
    }

    public Path tagPath() {
        return path.resolve(configuration().directory());
    }

    public Path relativize(Path path) {
        return this.path.relativize(path);
    }
}
