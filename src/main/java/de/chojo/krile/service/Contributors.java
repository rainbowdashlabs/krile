/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * The contributors of Krile on GitHub, fetched at most every five minutes.
 */
@Singleton
public class Contributors {
    private static final Logger log = getLogger(Contributors.class);
    private final HttpClient client =
            HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private final JsonMapper mapper = JsonMapper.builder()
            .changeDefaultVisibility(visibility -> visibility.withFieldVisibility(JsonAutoDetect.Visibility.ANY))
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
    private String contributors;
    private Instant lastFetch = Instant.MIN;

    /**
     * The contributors as markdown links, separated by commas. Bots are left out.
     *
     * @return the contributors
     */
    public synchronized String links() {
        if (contributors == null || lastFetch.isBefore(Instant.now().minus(5, ChronoUnit.MINUTES))) {
            contributors = fetch();
            lastFetch = Instant.now();
        }
        return contributors;
    }

    private String fetch() {
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://api.github.com/repos/rainbowdashlabs/krile/contributors?anon=1"))
                .header("accept", "application/vnd.github.v3+json")
                .header("User-Agent", "krile")
                .build();

        List<Contributor> contributors;
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            contributors = mapper.readerForListOf(Contributor.class).readValue(response.body());
        } catch (IOException | InterruptedException | JacksonException e) {
            log.error("Could not read response", e);
            contributors = Collections.emptyList();
        }

        List<GithubProfile> profiles = new ArrayList<>();
        for (var contributor : contributors) {
            if (ContributorType.BOT == contributor.type) continue;

            var profile = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(contributor.url))
                    .header("accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "krile")
                    .build();

            try {
                var response = client.send(profile, HttpResponse.BodyHandlers.ofString());
                profiles.add(mapper.readValue(response.body(), GithubProfile.class));
            } catch (IOException | InterruptedException | JacksonException e) {
                log.error("Could not read response", e);
            }
        }
        return profiles.stream().map(GithubProfile::toString).collect(Collectors.joining(", "));
    }

    @SuppressWarnings("unused")
    private enum ContributorType {
        @JsonProperty("User")
        USER,
        @JsonProperty("Bot")
        BOT
    }

    @SuppressWarnings("unused")
    private static class Contributor {
        private String login;
        private String url;

        @JsonProperty("html_url")
        private String htmlUrl;

        private ContributorType type;
    }

    @SuppressWarnings("unused")
    private static class GithubProfile {
        private String login;
        private String name;

        @JsonProperty("html_url")
        private String htmlUrl;

        @Override
        public String toString() {
            return String.format("[%s](%s)", name == null ? login : name, htmlUrl);
        }
    }
}
