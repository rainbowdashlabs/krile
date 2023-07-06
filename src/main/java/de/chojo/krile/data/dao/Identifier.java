package de.chojo.krile.data.dao;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

public record Identifier(String platform, String user, String repo, String path) {
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

    public static Identifier of(String platform, String user, String repo, @Nullable String path) {
        return new Identifier(platform.toLowerCase(), user, repo, path != null ? path.replaceAll("^/", "") : null);
    }

    public static Identifier of(String platform, String user, String repo) {
        return of(platform.toLowerCase(), user, repo, null);
    }

    public static Optional<Identifier> parse(String identifier) {
        String[] split = identifier.split("[:/]", 4);
        return switch (split.length) {
            case 3 -> Optional.of(Identifier.of(split[0], split[1], split[2]));
            case 4 -> Optional.of(Identifier.of(split[0], split[1], split[2], split[3]));
            default -> Optional.empty();
        };
    }
}
