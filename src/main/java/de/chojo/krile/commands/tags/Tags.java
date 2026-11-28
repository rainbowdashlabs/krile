/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.tags;

import com.google.inject.Inject;
import de.chojo.krile.commands.pagination.Pages;
import de.chojo.krile.commands.pagination.Pagination;
import de.chojo.krile.commands.tag.TagCompletion;
import de.chojo.krile.commands.tag.TagMessages;
import de.chojo.krile.data.access.GuildData;
import de.chojo.krile.data.dao.TagGuild;
import de.chojo.krile.data.dao.repository.tags.Tag;
import de.chojo.krile.util.Texts;
import io.github.kaktushose.jdac.annotations.interactions.AutoComplete;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.interactions.AutoCompleteEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Information about the tags of the repositories a guild uses.
 */
@Interaction("tags")
public class Tags {
    private static final int PAGE_SIZE = 10;
    private final GuildData guilds;

    @Inject
    public Tags(GuildData guilds) {
        this.guilds = guilds;
    }

    @Command(value = "info", desc = "Information about a tag")
    public void info(CommandEvent event, @Param(name = "tag", value = "name of tag") String tag) {
        Optional<Tag> resolved = guilds.guild(event.getGuild()).tags().resolveTag(tag);
        if (resolved.isEmpty()) {
            event.with().ephemeral(true).reply("error-tag-notfound");
            return;
        }
        event.with()
                .ephemeral(true)
                .builder(message -> message.setEmbeds(resolved.get().infoEmbed(Texts.of(event))))
                .reply();
    }

    @AutoComplete(value = "info", options = "tag")
    public void completeTag(AutoCompleteEvent event) {
        TagCompletion.complete(event, guilds.guild(event.getGuild()));
    }

    @Command(value = "list", desc = "A list of all tags on this server")
    public void list(CommandEvent event) {
        TagGuild guild = guilds.guild(event.getGuild());
        int pages = Math.max(1, Math.ceilDiv(guild.tags().count(), PAGE_SIZE));
        Pagination.reply(
                event,
                Pages.of(pages, (page, builder, texts) -> {
                    String tags = guild.tags().rankingPage(page, PAGE_SIZE).stream()
                            .map("%s"::formatted)
                            .collect(Collectors.joining("\n"));
                    builder.setEmbeds(new EmbedBuilder()
                            .setTitle(texts.get("words-tags"))
                            .setDescription(tags)
                            .build());
                }),
                true,
                List.of());
    }

    @Command(value = "random", desc = "Get a random tag")
    public void random(CommandEvent event) {
        TagGuild guild = guilds.guild(event.getGuild());
        Optional<Tag> random = guild.tags().random();
        if (random.isEmpty()) {
            event.reply("error-tag-notagsregistered");
            return;
        }
        guild.tags().used(random.get());
        TagMessages.show(event, random.get());
    }
}
