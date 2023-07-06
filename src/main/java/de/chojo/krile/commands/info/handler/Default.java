package de.chojo.krile.commands.info.handler;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.util.Colors;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.configuration.ConfigFile;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

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

public class Default implements SlashHandler {
    private static final String SOURCE = "[rainbowdashlabs/krile](https://github.com/rainbowdashlabs/krile)";
    private static final Logger log = getLogger(Default.class);
    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private final ObjectMapper mapper = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final String version;
    private final Configuration<ConfigFile> configuration;
    private String contributors;
    private Instant lastFetch = Instant.MIN;

    public Default(String version, Configuration<ConfigFile> configuration) {
        this.version = version;
        this.configuration = configuration;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        event.replyEmbeds(getResponse(event, context)).queue();
    }

    @NotNull
    private MessageEmbed getResponse(SlashCommandInteractionEvent event, EventContext context) {
        if (contributors == null || lastFetch.isBefore(Instant.now().minus(5, ChronoUnit.MINUTES))) {
            var request = HttpRequest.newBuilder().GET()
                    .uri(URI.create("https://api.github.com/repos/rainbowdashlabs/krile/contributors?anon=1"))
                    .header("accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "reputation-bot")
                    .build();

            List<Contributor> contributors;
            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                contributors = mapper.readerForListOf(Contributor.class).readValue(response.body());
            } catch (IOException | InterruptedException e) {
                log.error("Could not read response", e);
                contributors = Collections.emptyList();
            }

            List<GithubProfile> profiles = new ArrayList<>();
            for (var contributor : contributors) {
                if (ContributorType.BOT == contributor.type) continue;

                var profile = HttpRequest.newBuilder().GET()
                        .uri(URI.create(contributor.url))
                        .header("accept", "application/vnd.github.v3+json")
                        .header("User-Agent", "lyna")
                        .build();

                try {
                    var response = client.send(profile, HttpResponse.BodyHandlers.ofString());
                    profiles.add(mapper.readValue(response.body(), GithubProfile.class));
                } catch (IOException | InterruptedException e) {
                    log.error("Could not read response", e);
                }
            }
            this.contributors = profiles.stream().map(GithubProfile::toString).collect(Collectors.joining(", "));
            lastFetch = Instant.now();
        }

        return new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("Information about Krile")
                .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .addField("Contributor", contributors, false)
                .addField("Source", SOURCE, true)
                .addField("Version", version, true)
                .addField("", "**" + getLinks(context) + "**", false)
                .setColor(Colors.Pastel.BLUE)
                .build();
    }

    private String getLinks(EventContext context) {
        var links = List.of(
                getLink("Invite me", configuration.config().links().invite())
                //getLink("", configuration.config().support()),
                //getLink("TOS", configuration.config().tos()),
                //getLink( "Website", configuration.config().website()),
                //getLink("FAQ", configuration.config().faq())
        );
        return String.join(" ᠅ ", links);
    }

    private String getLink(String target, String url) {
        return "[%s](%s)".formatted(target, url);
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
