package de.chojo.krile.configuration.elements;

import de.chojo.jdautil.container.Pair;
import de.chojo.krile.data.dao.Identifier;
import org.intellij.lang.annotations.RegExp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record RepositoryLocation(String name, String template, String tld) {
    @RegExp
    private static final String PATH = "(?<user>.+?)/(?<repo>.+?)";
    public String url(String user, String repo) {
        return template.formatted("%s/%s".formatted(user, repo));
    }

    public boolean isUrl(String url) {
        return url.startsWith(tld) && url.endsWith(".git");
    }

    public Identifier extractIdentifier(String url){
        Pair<String, String> pair = parseUrl(url);
        return new Identifier(name, pair.first, pair.second);
    }

    public Pair<String, String> parseUrl(String url){
        Matcher matcher = Pattern.compile(template.formatted(PATH)).matcher(url);
        matcher.find();
        return Pair.of(matcher.group("user"), matcher.group("repo"));
    }

    public String url(Identifier identifier) {
        return url(identifier.user(), identifier.repo());
    }
}
