/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.tagimport.repo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.configuration.elements.RepositoryLocation;
import de.chojo.krile.data.dao.Identifier;
import de.chojo.krile.tagimport.exception.ImportException;
import de.chojo.krile.tagimport.exception.ParsingException;
import de.chojo.krile.tagimport.tag.RawTag;
import de.chojo.krile.tagimport.tag.parsing.TagParser;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private RepoConfig configuration;

    protected RawRepository(String url, Identifier identifier, Path root, Git git) {
        this.url = url;
        this.identifier = identifier;
        this.root = root;
        this.path = identifier.path() == null ? root : root.resolve(identifier.path());
        this.git = git;
    }

    /**
     * Returns the same repository but with a new identifier set.
     *
     * @param identifier new identifier
     * @return new repository instance
     * @throws IllegalArgumentException when the old and new identifier are not of the same url
     */
    public RawRepository updateIdentifier(Identifier identifier) {
        if (!this.identifier.equalsRepository(identifier)) {
            throw new IllegalArgumentException("Identifier %s and %s do not share the same url".formatted(this.identifier, identifier));
        }
        return new RawRepository(url, identifier, root, git);
    }

    public static RemoteRepository remote(Configuration<ConfigFile> configuration, Identifier identifier) throws ImportException, ParsingException {
        return remote(configuration, identifier, false);
    }

    public static RemoteRepository remote(Configuration<ConfigFile> configuration, Identifier identifier, boolean flat) throws ImportException, ParsingException {
        RepositoryLocation location = configuration.config().repositories().find(identifier).get();
        String url = location.url(identifier);
        Path git = null;
        try {
            git = Files.createTempDirectory("git");
        } catch (IOException e) {
            throw new ImportException("Could not create temporary file", e);
        }
        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(url)
                .setDirectory(git.toFile());
        if (flat) {
            log.info("Creating flat repo for {}", url);
            cloneCommand.setDepth(1);
        } else {
            log.info("Creating full repo for {}", url);
        }
        try {
            cloneCommand.call();
        } catch (GitAPIException e) {
            throw new ParsingException("Repository url is invalid " + url);
        }
        try {
            return new RemoteRepository(url, identifier, git, Git.open(git.toFile()));
        } catch (IOException e) {
            throw new ParsingException(e.getMessage(), e);
        }
    }

    public static RawRepository local(Path git, RepositoryLocation loc, Identifier identifier) throws IOException {
        String url = loc.url(identifier.user(), identifier.repo());
        return new RawRepository(url, identifier, git, Git.open(git.toFile()));
    }

    public RepoConfig configuration() throws ParsingException {
        if (configuration != null) return configuration;
        Path path = findConfigPath();
        try {
            configuration = MAPPER.readValue(path.toFile(), RepoConfig.class);
        } catch (IOException e) {
            log.error("Could not parse config file", e);
            throw new ParsingException("Could not parse configuration file.\n" + e.getMessage(), e);
        }
        return configuration;
    }

    public List<RawTag> tags() throws ParsingException, ImportException {
        RepoConfig configuration = configuration();
        try (var files = Files.list(tagPath())) {
            List<Path> list = files.filter(p -> p.toFile().isFile())
                    .filter(p -> p.toFile().getName().endsWith(".md"))
                    .filter(configuration::included)
                    .toList();
            List<RawTag> tags = new ArrayList<>();
            for (Path tagPath : list) {
                TagParser parse = TagParser.parse(this, tagPath);
                try {
                    tags.add(parse.tag());
                } catch (ParsingException e) {
                    log.error("Error while parsing file {}", tagPath.getFileName(), e);
                    throw new ParsingException("Failed to parse file %s%n%s".formatted(tagPath.getFileName(), e.getMessage()), e);
                }
            }
            return tags;
        } catch (IOException e) {
            throw new ImportException(e);
        }
    }

    public Path tagPath() throws ParsingException {
        return path.resolve(configuration().directory());
    }

    public Path relativize(Path path) {
        return this.root.relativize(path);
    }

    public String currentCommit() throws ImportException {
        try {
            return git().getRepository().resolve("HEAD").getName();
        } catch (IOException e) {
            throw new ImportException("Could not determine commit", e);
        }
    }

    public String currentBranch() throws ImportException {
        try {
            return git().getRepository().getBranch();
        } catch (IOException e) {
            throw new ImportException("Could not determine branch", e);
        }
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

    private Path findConfigPath() throws ParsingException {
        // TODO: Probably a bot config value
        final String[] paths = new String[]{".krile", ".github", ".gitlab", ""};
        final String[] files = new String[]{"krile.yaml", "krile.yml", "krile.json"};
        for (String currPath : paths) {
            for (String currFile : files) {
                Path resolved = path.resolve(currPath).resolve(currFile);
                if (resolved.toFile().exists()) return resolved;
            }
        }
        throw new ParsingException("No configuration file found. Create a file called krile.yaml, krile.yml or krile.json");
    }
}
