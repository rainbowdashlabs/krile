package de.chojo.krile.data.dao;

import java.util.Locale;
import java.util.Optional;

public record Identifier(String platform, String user, String repo) {
    @Override
    public String toString() {
        return "%s:%s/%s".formatted(platform.toLowerCase(Locale.ROOT), user, repo);
    }

    public static Optional<Identifier> parse(String identifier) {
        String[] split = identifier.split("[:/]");
        if(split.length != 3) return Optional.empty();
        return Optional.of(new Identifier(split[0], split[1], split[2]));
    }
}
