package de.chojo.krile.configuration.elements;

import java.util.List;
import java.util.Optional;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})

public class Repositories {
private List<RepositoryLocation> repositories = List.of(
            new RepositoryLocation("GitHub", "https://github.com/%s.git", "https://github.com"),
            new RepositoryLocation("GitLab", "https://gitlab.com/%s.git", "https://gitlab.com"));

    public List<RepositoryLocation> repositories() {
        return repositories;
    }

    public Optional<RepositoryLocation> find(String url) {
        for (RepositoryLocation repositoryLocation : repositories) {
            if (repositoryLocation.isUrl(url)) {
                return Optional.of(repositoryLocation);
            }
        }
        return Optional.empty();
    }
}
