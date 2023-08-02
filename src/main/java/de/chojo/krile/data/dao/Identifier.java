/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao;

import org.jetbrains.annotations.Nullable;

import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.toIdentityString;

public record Identifier(String platform, String user, String repo, String path) {
    /**
     * Creates a new Identifier object with the specified platform, user, repo, and optional path.
     *
     * @param platform the platform of the identifier
     * @param user     the user of the identifier
     * @param repo     the repository of the identifier
     * @param path     the optional path associated with the identifier
     * @return the new Identifier object
     */
    public static Identifier of(String platform, String user, String repo, @Nullable String path) {
        return new Identifier(platform.toLowerCase(), requireNonNull(user), requireNonNull(repo), path != null ? path.replaceAll("^/", "") : null);
    }

    public static Identifier of(String platform, String user, String repo) {
        return of(platform.toLowerCase(), user, repo, null);
    }

    /**
     * Parses the given identifier string and returns an Optional containing the Identifier object.
     *
     * @param identifier the identifier string to parse
     * @return an Optional containing the parsed Identifier object, or an empty Optional if the identifier string is invalid
     * @throws IllegalFormatException if the identifier string is null or empty
     */
    public static Optional<Identifier> parse(String identifier) throws IllegalFormatException {
        String identity = identifier;
        String path = null;
        if (identifier.contains("//")) {
            var split = identifier.split("//", 2);
            identity = split[0];
            path = split[1];
        }
        String[] split = identity.split("[:/]", 3);
        if (split.length == 3) {
            if (path != null) return Optional.of(Identifier.of(split[0], split[1], split[2], path));
            return Optional.of(Identifier.of(split[0], split[1], split[2]));
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        if (path != null) {
            return "%s:%s/%s//%s".formatted(platform.toLowerCase(Locale.ROOT), user, repo, path);
        }
        return "%s:%s/%s".formatted(platform.toLowerCase(Locale.ROOT), user, repo);
    }

    public String name() {
        return "%s/%s".formatted(user, repo);
    }

    public boolean equalsRepository(Identifier o) {
        if (!o.platform.equals(platform)) return false;
        if (!o.user.equals(user)) return false;
        return o.repo.equals(repo);
    }
}
