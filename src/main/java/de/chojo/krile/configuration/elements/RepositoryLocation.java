/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.configuration.elements;

import de.chojo.jdautil.container.Pair;
import de.chojo.krile.data.dao.Identifier;
import org.intellij.lang.annotations.RegExp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record RepositoryLocation(String name, String template, String tld, String filePath, String dirPath) {

    public String url(String user, String repo) {
        return template.replace("{user}", user).replace("{repo}", repo);
    }

    public boolean isUrl(String url) {
        return url.startsWith(tld) && url.endsWith(".git");
    }

    public Identifier extractIdentifier(String url) {
        Pair<String, String> pair = parseUrl(url);
        return Identifier.of(name, pair.first, pair.second);
    }

    public Pair<String, String> parseUrl(String url) {
        Matcher matcher = Pattern.compile(url("(?<user>.+?)", "(?<repo>.+?)")).matcher(url);
        matcher.find();
        return Pair.of(matcher.group("user"), matcher.group("repo"));
    }

    public String filePath(String user, String repo, String branch, String path) {
        return filePath.replace("{user}", user).replace("{repo}", repo).replace("{branch}", branch).replace("{path}", path);
    }

    public String dirPath(String user, String repo, String branch, String path) {
        return dirPath.replace("{user}", user).replace("{repo}", repo).replace("{branch}", branch).replace("{path}", path);
    }

    public String url(Identifier identifier) {
        return url(identifier.user(), identifier.repo());
    }
}
