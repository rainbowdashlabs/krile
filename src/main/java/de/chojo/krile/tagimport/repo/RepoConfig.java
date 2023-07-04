package de.chojo.krile.tagimport.repo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;

// TODO: Idea: Parent repositories, to directly include them.
// Also add a priority for this repo and parent repos to allow overwriting tags. (Probably too complex),
public record RepoConfig(@Nullable String name,
                         @Nullable String description,
                         String directory,
                         List<String> category,
                         boolean publicRepo,
                         @Nullable String language,
                         List<String> include,
                         List<String> exclude
) {
    public static final RepoConfig DEFAULT = new RepoConfig(null, null, "", emptyList(), false, null, emptyList(), emptyList());

    @JsonCreator
    public static RepoConfig create(@JsonProperty("name") String name,
                                    @JsonProperty("description") String description,
                                    @JsonProperty("category") List<String> category,
                                    @JsonProperty("public") Boolean publicRepo,
                                    @JsonProperty("language") String language,
                                    @JsonProperty("directory") String directory,
                                    @JsonProperty("include") List<String> include,
                                    @JsonProperty("exclude") List<String> exclude) {
        return new RepoConfig(name,
                description,
                requireNonNullElse(directory, ""),
                requireNonNullElse(category, emptyList()),
                requireNonNullElse(publicRepo, false),
                language,
                requireNonNullElse(include, emptyList()),
                requireNonNullElse(exclude, emptyList()));
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
