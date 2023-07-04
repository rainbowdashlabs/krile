package de.chojo.krile.tagimport.repo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.configuration.elements.RepositoryLocation;
import de.chojo.krile.data.dao.Identifier;
import de.chojo.krile.tagimport.tag.RawTag;
import de.chojo.krile.tagimport.tag.parsing.TagParser;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class RawRepository {
    private static final ObjectMapper MAPPER = YAMLMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .build();
    private static final Logger log = getLogger(RawRepository.class);
    private final String url;
    private final Identifier identifier;
    private final Path root;
    private final Git git;
    private final Path path;

    protected RawRepository(String url, Identifier identifier, Path root, Git git) {
        this.url = url;
        this.identifier = identifier;
        this.root = root;
        this.path = identifier.path() == null ? root : root.resolve(identifier.path());
        this.git = git;
    }

    public static RawRepository remote(Configuration<ConfigFile> configuration, Identifier identifier) throws IOException, GitAPIException {
        RepositoryLocation location = configuration.config().repositories().find(identifier).get();
        String url = location.url(identifier);
        log.info("Creating repo for {}", url);
        Path git = Files.createTempDirectory("git");
        Git.cloneRepository()
                .setURI(url)
                .setDirectory(git.toFile())
                .call();
        return new RemoteRepository(url, identifier, git, Git.open(git.toFile()));
    }

    public static RawRepository root(Path git, RepositoryLocation loc, String user, String repo) throws IOException {
        String url = loc.url(user, repo);
        return new RawRepository(url, Identifier.of(loc.name(), user, repo), git, Git.open(git.toFile()));
    }

    public static RawRepository sub(Path git, RepositoryLocation loc, String path, String user, String repo) throws IOException {
        String url = loc.url(user, repo);
        return new RawRepository(url, Identifier.of(loc.name(), user, repo, path), git, Git.open(git.toFile()));
    }

    public RepoConfig configuration() {
        // TODO: Lazy loading and cache
        Optional<Path> path = findConfigPath();
        if (path.isPresent()) {
            try {
                return MAPPER.readValue(path.get().toFile(), RepoConfig.class);
            } catch (IOException e) {
                log.error("Could not parse config file", e);
                // ignore
            }
        }
        return RepoConfig.DEFAULT;
    }

    private Optional<Path> findConfigPath() {
        // TODO: Probably a bot config value
        final String[] paths = new String[]{".krile", ".github", ".gitlab", ""};
        final String[] files = new String[]{"krile.yaml", "krile.yml", "krile.json"};
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


    public Path tagPath() {
        return path.resolve(configuration().directory());
    }

    public Path relativize(Path path) {
        return this.root.relativize(path);
    }

    public String currentCommit() throws IOException {
        return git().getRepository().resolve("HEAD").getName();
    }

    public String url() {
        return url;
    }

    public Identifier identifier() {
        return identifier;
    }

    public Path root() {
        return root;
    }
public Path path() {
        return path;
    }

    public Git git() {
        return git;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RawRepository) obj;
        return Objects.equals(this.url, that.url) &&
                Objects.equals(this.identifier, that.identifier) &&
                Objects.equals(this.root, that.root) &&
                Objects.equals(this.git, that.git);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, identifier, root, git);
    }

    @Override
    public String toString() {
        return "RawRepository[" +
                "url=" + url + ", " +
                "identifier=" + identifier + ", " +
                "path=" + root + ", " +
                "git=" + git + ']';
    }
}
