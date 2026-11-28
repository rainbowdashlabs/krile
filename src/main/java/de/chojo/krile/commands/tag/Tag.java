/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.tag;

import com.google.inject.Inject;
import de.chojo.krile.data.access.GuildData;
import de.chojo.krile.data.dao.TagGuild;
import io.github.kaktushose.jdac.annotations.interactions.AutoComplete;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.interactions.AutoCompleteEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Shows a tag of the repositories the guild uses.
 */
@Interaction("tag")
public class Tag {
    private final GuildData guilds;

    @Inject
    public Tag(GuildData guilds) {
        this.guilds = guilds;
    }

    @Command(desc = "Retrieve a tag")
    public void show(CommandEvent event, @Param(name = "tag", value = "Get a tag") String tag) {
        event.deferReply(false);
        TagGuild guild = guilds.guild(event.getGuild());
        Optional<de.chojo.krile.data.dao.repository.tags.Tag> resolved =
                guild.tags().resolveTag(tag);
        if (resolved.isEmpty()) {
            event.reply("error-tag-notfound");
            event.jdaEvent().getHook().deleteOriginal().queueAfter(1, TimeUnit.MINUTES);
            return;
        }
        guild.tags().used(resolved.get());
        TagMessages.show(event, resolved.get());
    }

    @AutoComplete(value = "show", options = "tag")
    public void completeTag(AutoCompleteEvent event) {
        TagCompletion.complete(event, guilds.guild(event.getGuild()));
    }
}
