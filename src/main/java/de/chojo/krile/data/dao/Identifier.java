/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao;

import org.jetbrains.annotations.Nullable;

import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public record Identifier(String platform, String user, String repo, String path) {
    public static Identifier of(String platform, String user, String repo, @Nullable String path) {
        return new Identifier(platform.toLowerCase(), requireNonNull(user), requireNonNull(repo), path != null ? path.replaceAll("^/", "") : null);
    }

    public static Identifier of(String platform, String user, String repo) {
        return of(platform.toLowerCase(), user, repo, null);
    }

    public static Optional<Identifier> parse(String identifier) throws IllegalFormatException {
        String[] split = identifier.split("[:/]", 4);
        return switch (split.length) {
            case 3 -> Optional.of(Identifier.of(split[0], split[1], split[2]));
            case 4 -> Optional.of(Identifier.of(split[0], split[1], split[2], split[3]));
            default -> Optional.empty();
        };
    }

    @Override
    public String toString() {
        if (path != null) {
            return "%s:%s/%s/%s".formatted(platform.toLowerCase(Locale.ROOT), user, repo, path);
        }
        return "%s:%s/%s".formatted(platform.toLowerCase(Locale.ROOT), user, repo);
    }

    public String name() {
        return "%s/%s".formatted(user, repo);
    }
}
