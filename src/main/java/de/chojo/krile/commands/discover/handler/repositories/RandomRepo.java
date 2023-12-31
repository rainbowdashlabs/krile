/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.discover.handler.repositories;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.data.dao.Repository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Optional;

public class RandomRepo implements SlashHandler {
    private final RepositoryData repositoryData;

    public RandomRepo(RepositoryData repositoryData) {
        this.repositoryData = repositoryData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Optional<Repository> random = repositoryData.random();
        if (random.isEmpty()) {
            event.reply(context.localize("error.repository.notfound")).setEphemeral(true).queue();
            return;
        }
        event.replyEmbeds(random.get().infoEmbed(context)).setEphemeral(true).queue();
    }
}
