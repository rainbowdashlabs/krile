/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.tag;

import de.chojo.krile.data.dao.TagGuild;
import io.github.kaktushose.jdac.dispatching.events.interactions.AutoCompleteEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

/**
 * Completes tag names of the repositories a guild uses.
 */
public final class TagCompletion {
    private TagCompletion() {}

    /**
     * Replies with the tags matching the typed value, named by tag and carrying the tag id.
     *
     * @param event autocomplete event
     * @param guild guild whose tags are offered
     */
    public static void complete(AutoCompleteEvent event, TagGuild guild) {
        event.replyChoices(guild.tags().complete(event.getValue()).stream()
                .map(tag -> new Command.Choice(tag.name(), tag.id()))
                .toList());
    }
}
