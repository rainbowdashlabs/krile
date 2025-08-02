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
import de.chojo.jdautil.configuration.Configuration;
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
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * The RawRepository class represents a repository with raw data.
 * It allows retrieval of repository information and manipulation of its identifier.
 */
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

    /**
     * Creates a remote repository with the specified configuration and identifier.
     *
     * @param configuration the configuration of the remote repository
     * @param identifier the identifier of the remote repository
     * @return a RemoteRepository instance representing the remote repository
     * @throws ImportException if there is an error importing the remote repository
     * @throws ParsingException if there is an error parsing the remote repository
     */
    public static RemoteRepository remote(Configuration<ConfigFile> configuration, Identifier identifier) throws ImportException, ParsingException {
        return remote(configuration, identifier, false);
    }

    /**
     * Creates a remote repository with the specified configuration and identifier.
     *
     * @param configuration the configuration of the remote repository
     * @param identifier the identifier of the remote repository
     * @param flat indicating if a flat repository should be created
     * @return a RemoteRepository instance representing the remote repository
     * @throws ImportException if there is an error importing the remote repository
     * @throws ParsingException if there is an error parsing the remote repository
     */
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

    /**
     * Creates a local repository with the specified git path, repository location, and identifier.
     *
     * @param git the path to the local git repository
     * @param loc the location of the repository
     * @param identifier the identifier of the local repository
     * @return a RawRepository instance representing the local repository
     * @throws IOException if there is an error opening the local git repository
     */
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

    /**
     * Retrieves a list of RawTags for the repository.
     *
     * @return a List of RawTag instances representing the tags in the repository
     * @throws ParsingException if there is an error parsing the tag files
     * @throws ImportException if there is an error importing the tag files
     */
    public List<RawTag> tags() throws ParsingException, ImportException {
        RepoConfig configuration = configuration();
        try (var files = listFiles(tagPath(), configuration.deep())) {
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

    /**
     * Returns a stream of file paths for the specified directory.
     *
     * @param path the directory path
     * @param deep indicates whether subdirectories should be included in the search
     * @return a stream of Path instances representing the files in the directory
     * @throws IOException if an I/O error occurs while listing the files
     */
    private Stream<Path> listFiles(Path path, boolean deep) throws IOException {
        return deep ? Files.walk(path) : Files.list(path);
    }

    /**
     * Resolves the path of the specified directory using the configuration directory value.
     *
     * @return the resolved path of the directory
     * @throws ParsingException if an error occurs while parsing the directory configuration
     */
    public Path tagPath() throws ParsingException {
        return path.resolve(configuration().directory());
    }

    /**
     * Relativizes the specified path relative to the root path.
     *
     * @param path the path to relativize
     * @return the relative path of the specified path
     */
    public Path relativize(Path path) {
        return this.root.relativize(path);
    }

    /**
     * Returns the name of the current commit in the Git repository.
     *
     * @return the name of the current commit
     * @throws ImportException if an error occurs while determining the commit
     */
    public String currentCommit() throws ImportException {
        try {
            return git().getRepository().resolve("HEAD").getName();
        } catch (IOException e) {
            throw new ImportException("Could not determine commit", e);
        }
    }

    /**
     * Returns the name of the current branch in the Git repository.
     *
     * @return the name of the current branch
     * @throws ImportException if an error occurs while determining the branch
     */
    public String currentBranch() throws ImportException {
        try {
            return git().getRepository().getBranch();
        } catch (IOException e) {
            throw new ImportException("Could not determine branch", e);
        }
    }

    /**
     * Returns the URL of the Git repository.
     *
     * @return the URL of the Git repository
     */
    public String url() {
        return url;
    }

    /**
     * Returns the identifier of the method.
     *
     * @return the identifier of the method
     */
    public Identifier identifier() {
        return identifier;
    }

    /**
     * Returns the root path of the repository.
     *
     * @return the root path of the repository
     */
    public Path root() {
        return root;
    }

    /**
     * Returns the current path of the repository.
     *
     * @return the current path of the repository
     */
    public Path path() {
        return path;
    }

    /**
     * Returns the Git object associated with the repository.
     *
     * @return the Git object associated with the repository
     */
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

    /**
     * Finds the path of the configuration file for the repository.
     *
     * @return the path of the configuration file
     * @throws ParsingException if no configuration file is found
     */
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
