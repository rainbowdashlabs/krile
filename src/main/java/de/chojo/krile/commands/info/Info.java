/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.info;

import com.google.inject.Inject;
import de.chojo.krile.configuration.Configuration;
import de.chojo.krile.configuration.Links;
import de.chojo.krile.service.Contributors;
import de.chojo.krile.util.Texts;
import dev.chojo.aether.common.util.Colors;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.CommandConfig;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Shows the version, the contributors and the links of Krile.
 */
@Interaction("info")
@CommandConfig(context = {InteractionContextType.GUILD, InteractionContextType.BOT_DM})
public class Info {
    private static final Logger log = getLogger(Info.class);
    private static final String SOURCE = "[rainbowdashlabs/krile](https://github.com/rainbowdashlabs/krile)";
    private static final String VERSION = readVersion();
    private final Configuration configuration;
    private final Contributors contributors;

    @Inject
    public Info(Configuration configuration, Contributors contributors) {
        this.configuration = configuration;
        this.contributors = contributors;
    }

    @Command(desc = "Bot information")
    public void info(CommandEvent event) {
        Texts texts = Texts.of(event);
        event.reply(new EmbedBuilder()
                .setTitle(texts.get("command-info-message-title"))
                .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .addField(texts.get("words-contributor"), contributors.links(), false)
                .addField(texts.get("words-sourcecode"), SOURCE, true)
                .addField(texts.get("words-version"), VERSION, true)
                .addField("", "**" + links(texts) + "**", false)
                .setColor(Colors.Pastel.BLUE)
                .build());
    }

    private String links(Texts texts) {
        Links links = configuration.main().links();
        return String.join(
                " ᠅ ",
                List.of(
                        link(texts.get("phrases-inviteme"), links.invite()),
                        link(texts.get("words-support"), links.support()),
                        link(texts.get("words-website"), links.website())));
    }

    private static String link(String text, String url) {
        return "[%s](%s)".formatted(text, url);
    }

    private static String readVersion() {
        try (var input = Info.class.getClassLoader().getResourceAsStream("version")) {
            if (input == null) return "undefined";
            return new String(input.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            log.error("Could not determine version.");
            return "undefined";
        }
    }
}
