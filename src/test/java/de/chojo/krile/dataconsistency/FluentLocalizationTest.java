/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.dataconsistency;

import dev.goldmensch.fluava.Bundle;
import dev.goldmensch.fluava.Fluava;
import dev.goldmensch.fluava.Message;
import dev.goldmensch.fluava.Resource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the Fluent files of the default bundle: both parse, define the same keys, every message has text, every
 * translation uses the same placeholders as the English message, and every key used in the code exists.
 */
class FluentLocalizationTest {
    private static final Path BUNDLE = Path.of("src", "main", "resources", "locale", "default");
    private static final Pattern KEY = Pattern.compile("^([a-zA-Z][a-zA-Z0-9_-]*) *=", Pattern.MULTILINE);
    private static final Pattern CODE_KEY = Pattern.compile("\"((?:command|error|embeds|words|phrases)-[a-z0-9-]+)\"");
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{ *\\$([a-zA-Z0-9_-]+) *}");

    @Test
    void germanMatchesEnglish() throws IOException {
        String english = read("en");
        String german = read("de");

        assertEquals(keys(english), keys(german));
        for (String key : keys(english)) {
            assertEquals(
                    placeholders(entry(english, key)),
                    placeholders(entry(german, key)),
                    "Placeholders differ in de for " + key);
        }
    }

    @Test
    void everyMessageResolves() throws IOException {
        for (String language : new String[] {"en", "de"}) {
            String content = read(language);
            Resource resource = Fluava.create(Locale.ENGLISH)
                    .of(content, Locale.forLanguageTag(language))
                    .orElseThrow();
            for (String key : keys(content)) {
                Message message = resource.message(key);
                assertFalse(message.notFound(), "Missing " + key + " in " + language);
                Map<String, Object> arguments = placeholders(entry(content, key)).stream()
                        .collect(Collectors.toMap(Function.identity(), name -> "value"));
                assertFalse(message.apply(arguments).isBlank(), "Blank " + key + " in " + language);
            }
        }
    }

    @Test
    void bundleFindsEveryMessage() throws IOException {
        Bundle bundle = Fluava.builder()
                .bundleRoot("locale")
                .fallback(Locale.ENGLISH)
                .build()
                .loadBundle("default");
        for (String key : keys(read("en"))) {
            assertFalse(bundle.message(Locale.forLanguageTag("en-US"), key).notFound(), "Missing " + key + " in en-US");
            assertFalse(bundle.message(Locale.GERMAN, key).notFound(), "Missing " + key + " in de");
        }
    }

    @Test
    void everyKeyInCodeExists() throws IOException {
        Set<String> keys = keys(read("en"));
        List<Path> sources;
        try (var files = Files.walk(Path.of("src", "main", "java"))) {
            sources = files.filter(file -> file.toString().endsWith(".java")).toList();
        }
        int found = 0;
        for (Path source : sources) {
            Matcher matcher = CODE_KEY.matcher(Files.readString(source));
            while (matcher.find()) {
                found++;
                assertTrue(keys.contains(matcher.group(1)), "Unknown key " + matcher.group(1) + " in " + source);
            }
        }
        assertTrue(found > 0, "No keys found in the code");
    }

    private static String read(String language) throws IOException {
        return Files.readString(BUNDLE.resolve(language + ".ftl"));
    }

    private static Set<String> keys(String content) {
        Set<String> keys = new LinkedHashSet<>();
        Matcher matcher = KEY.matcher(content);
        while (matcher.find()) keys.add(matcher.group(1));
        return keys;
    }

    private static String entry(String content, String key) {
        Matcher matcher = Pattern.compile(
                        "^" + Pattern.quote(key) + " *=(.*?)(?=^[a-zA-Z]|\\z)", Pattern.MULTILINE | Pattern.DOTALL)
                .matcher(content);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static Set<String> placeholders(String text) {
        Set<String> found = new LinkedHashSet<>();
        Matcher matcher = PLACEHOLDER.matcher(text);
        while (matcher.find()) found.add(matcher.group(1));
        return found;
    }
}
