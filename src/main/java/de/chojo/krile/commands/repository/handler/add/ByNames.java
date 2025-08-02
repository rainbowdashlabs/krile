/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.repository.handler.add;

import de.chojo.jdautil.configuration.Configuration;
import de.chojo.jdautil.util.Choice;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.configuration.elements.RepositoryLocation;
import de.chojo.krile.data.access.GuildData;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.data.dao.Identifier;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;
import java.util.Optional;

public class ByNames extends BaseAdd {

    public ByNames(RepositoryData repositoryData, GuildData guilds, Configuration<ConfigFile> configuration) {
        super(repositoryData, guilds, configuration);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        String platform = event.getOption("platform", OptionMapping::getAsString);
        String name = event.getOption("user", OptionMapping::getAsString);
        String repo = event.getOption("repository", OptionMapping::getAsString);
        String path = event.getOption("path", OptionMapping::getAsString);
        Identifier identifier = Identifier.of(platform, name, repo, path);
        Optional<RepositoryLocation> optLocation = configuration().config().repositories().find(identifier);
        if (optLocation.isEmpty()) {
            event.reply(context.localize("error.source.invalid")).setEphemeral(true).queue();
            return;
        }
        add(event, context, identifier);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        AutoCompleteQuery option = event.getFocusedOption();
        if (option.getName().equals("platform")) {
            List<Command.Choice> complete = Completion.complete(option.getValue(), configuration().config().repositories().repositories().stream().map(RepositoryLocation::name));
            event.replyChoices(complete).queue();
            return;
        }
        if (option.getName().equals("name")) {
            event.replyChoices(Choice.toStringChoice(repositoryData().completeUser(option.getValue()))).queue();
            return;
        }
        if (option.getName().equals("repo")) {
            event.replyChoices(Choice.toStringChoice(repositoryData().completeRepository(option.getValue()))).queue();
            return;
        }
        if (option.getName().equals("path")) {
            event.replyChoices(Choice.toStringChoice(repositoryData().completePath(option.getValue()))).queue();
        }
    }
}
