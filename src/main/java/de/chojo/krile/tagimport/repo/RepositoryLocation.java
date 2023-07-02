package de.chojo.krile.tagimport.repo;

import java.util.function.Predicate;

public enum RepositoryLocation {
    GITHUB("https://github.com/%s.git", s -> s.startsWith("https://github.com") && s.endsWith(".git"));
//    GITLAB("https://gitlab.com/%s.git", s -> s.startsWith("https://gitlab.com") && s.endsWith(".git"));

    private final String url;
    private final Predicate<String> isUrl;

    RepositoryLocation(String url, Predicate<String> isUrl) {
        this.url = url;
        this.isUrl = isUrl;
    }

    public String url(String user, String repo) {
        return this.url.formatted("%s/%s".formatted(user, repo));
    }

    public boolean isUrl(String url) {
        return this.isUrl.test(url);
    }
}
