/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.tags.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.commands.tag.handler.Show;
import de.chojo.krile.data.access.GuildData;
import de.chojo.krile.data.dao.repository.tags.Tag;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Optional;

public class Random implements SlashHandler {
    private final GuildData guilds;

    public Random(GuildData guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Optional<Tag> random = guilds.guild(event).tags().random();
        if (random.isEmpty()) {
            event.reply(context.localize("error.tag.notagsregistered")).queue();
            return;
        }
        Show.showTag(event, context, guilds.guild(event), random.get());
    }
}
