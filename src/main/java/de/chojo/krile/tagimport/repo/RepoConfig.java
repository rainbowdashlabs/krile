/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

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
                         List<String> exclude,
                         boolean deep
) {

    /**
     * Creates a new {@code RepoConfig} object with the given parameters.
     *
     * @param name        the name of the repository
     * @param description the description of the repository
     * @param category    the category of the repository
     * @param publicRepo  the visibility of the repository
     * @param language    the language of the repository
     * @param directory   the directory of the repository
     * @param include     the list of files to include in the repository
     * @param exclude     the list of files to exclude from the repository
     * @param deep        whether to include subdirectories in the repository
     * @return a new {@code RepoConfig} object with the given parameters
     */
    @JsonCreator
    public static RepoConfig create(@JsonProperty("name") String name,
                                    @JsonProperty("description") String description,
                                    @JsonProperty("category") List<String> category,
                                    @JsonProperty("public") Boolean publicRepo,
                                    @JsonProperty("language") String language,
                                    @JsonProperty("directory") String directory,
                                    @JsonProperty("include") List<String> include,
                                    @JsonProperty("exclude") List<String> exclude,
                                    @JsonProperty("deep") Boolean deep) {
        return new RepoConfig(name,
                description,
                requireNonNullElse(directory, ""),
                requireNonNullElse(category, emptyList()),
                requireNonNullElse(publicRepo, false),
                language,
                requireNonNullElse(include, emptyList()),
                requireNonNullElse(exclude, emptyList()),
                requireNonNullElse(deep, false));
    }

    /**
     * Checks if the given file path is included in the repository.
     *
     * @param path the path of the file to check
     * @return true if the file is included in the repository, false otherwise
     */
    public boolean included(Path path) {
        String name = path.toFile().getName();
        if (!name.endsWith(".md")) return false;
        name = name.replace(".md", "");
        if (!include.isEmpty()) return include.contains(name);
        if (!exclude.isEmpty()) return !exclude.contains(name);
        return true;
    }
}
