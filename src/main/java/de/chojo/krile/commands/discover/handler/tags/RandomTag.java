/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.discover.handler.tags;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.commands.tag.handler.Show;
import de.chojo.krile.data.access.TagData;
import de.chojo.krile.data.dao.repository.tags.Tag;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Optional;

public class RandomTag implements SlashHandler {
    private final TagData tagData;

    public RandomTag(TagData tagData) {
        this.tagData = tagData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Optional<Tag> random = tagData.random();
        if (random.isEmpty()) {
            event.reply(context.localize("error.tag.notfound")).setEphemeral(true).queue();
            return;
        }
        Show.showTag(event, context, random.get());
        event.replyEmbeds(random.get().infoEmbed(context.guildLocalizer())).setEphemeral(true).queue();
    }
}
