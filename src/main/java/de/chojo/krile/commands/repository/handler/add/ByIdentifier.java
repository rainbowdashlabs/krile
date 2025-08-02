/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.repository.handler.add;

import de.chojo.jdautil.configuration.Configuration;
import de.chojo.jdautil.util.Choice;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.configuration.elements.RepositoryLocation;
import de.chojo.krile.data.access.GuildData;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.data.dao.Identifier;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Optional;

public class ByIdentifier extends BaseAdd {

    public ByIdentifier(RepositoryData repositoryData, GuildData guilds, Configuration<ConfigFile> configuration) {
        super(repositoryData, guilds, configuration);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        String identifier = event.getOption("identifier", OptionMapping::getAsString);
        Optional<Identifier> parsed = Identifier.parse(identifier);
        if (parsed.isEmpty()) {
            event.reply(context.localize("error.identifier.invalid")).setEphemeral(true).queue();
            return;
        }
        Optional<RepositoryLocation> optLocation = configuration().config().repositories().find(parsed.get());
        if (optLocation.isEmpty()) {
            event.reply(context.localize("error.source.invalid")).setEphemeral(true).queue();
            return;
        }
        add(event, context, parsed.get());
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        event.replyChoices(Choice.toStringChoice(repositoryData().completeIdentifier(event.getFocusedOption().getValue()))).queue();
    }
}
