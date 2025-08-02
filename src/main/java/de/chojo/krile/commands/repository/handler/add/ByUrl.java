/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.repository.handler.add;

import de.chojo.jdautil.configuration.Configuration;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.configuration.elements.RepositoryLocation;
import de.chojo.krile.data.access.GuildData;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.data.dao.Identifier;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Optional;

public class ByUrl extends BaseAdd {

    public ByUrl(RepositoryData repositoryData, GuildData guilds, Configuration<ConfigFile> configuration) {
        super(repositoryData, guilds, configuration);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        String url = event.getOption("url", OptionMapping::getAsString);
        Optional<RepositoryLocation> optLocation = configuration().config().repositories().find(url);
        if (optLocation.isEmpty()) {
            event.reply(context.localize("error.url.invalidsource")).setEphemeral(true).queue();
            return;
        }

        Identifier identifier = optLocation.get().extractIdentifier(url);
        add(event, context, identifier);
    }
}
