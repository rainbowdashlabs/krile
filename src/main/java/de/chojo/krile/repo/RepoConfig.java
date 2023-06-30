package de.chojo.krile.repo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNullElse;

public record RepoConfig(String directory,
                         List<String> include,
                         List<String> exclude
) {
    public static final RepoConfig DEFAULT = new RepoConfig("", Collections.emptyList(), Collections.emptyList());

    @JsonCreator
    public static RepoConfig create(@JsonProperty("directory") String directory,
                                    @JsonProperty("include") List<String> include,
                                    @JsonProperty("exclude") List<String> exclude) {
        return new RepoConfig(requireNonNullElse(directory, ""),
                requireNonNullElse(include, Collections.emptyList()),
                requireNonNullElse(exclude, Collections.emptyList()));
    }

    public boolean included(Path p) {
        String name = p.toFile().getName();
        if (!name.endsWith(".md")) return false;
        name = name.replace(".md", "");
        if (!include.isEmpty()) return include.contains(name);
        if (!exclude.isEmpty()) return !exclude.contains(name);
        return true;
    }
}
