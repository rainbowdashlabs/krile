package de.chojo.krile.configuration.elements;

import de.chojo.krile.data.dao.Identifier;

import java.util.List;
import java.util.Optional;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})

public class Repositories {
    public static final RepositoryLocation GITHUB = new RepositoryLocation(
            "GitHub",
            "https://github.com/{user}/{repo}.git",
            "https://github.com",
            "https://github.com/{user}/{repo}/blob/{branch}/{path}",
            "https://github.com/{user}/{repo}/tree/{branch}/{path}"
    );
    public static final RepositoryLocation GITLAB = new RepositoryLocation(
            "GitLab",
            "https://gitlab.com/{user}/{repo}.git",
            "https://gitlab.com",
            "https://gitlab.com/{user}/{repo}/-/blob/{branch}/{path}",
            "https://gitlab.com/{user}/{repo}/-/tree/{branch}/{path}"
    );
    private List<RepositoryLocation> repositories = List.of(GITHUB, GITLAB);
    /**
     * Minutes after which a repository will be checked automatically
     */
    private int check = 60;
    /**
     * Minutes after which a repository can be scheduled to be checked again.
     */
    private int minCheck = 5;

    public List<RepositoryLocation> repositories() {
        return repositories;
    }

    public int check() {
        return check;
    }

    public int minCheck() {
        return minCheck;
    }

    public Optional<RepositoryLocation> find(String url) {
        for (RepositoryLocation repositoryLocation : repositories) {
            if (repositoryLocation.isUrl(url)) {
                return Optional.of(repositoryLocation);
            }
        }
        return Optional.empty();
    }

    public Optional<RepositoryLocation> find(Identifier identifier) {
        for (RepositoryLocation repository : repositories) {
            if (repository.name().equalsIgnoreCase(identifier.platform())) {
                return Optional.of(repository);
            }
        }
        return Optional.empty();
    }
}
