/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.repository.handler.remove;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.data.access.GuildData;
import de.chojo.krile.data.dao.tagguild.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;
import java.util.Optional;

public class Remove implements SlashHandler {
    private final GuildData guilds;

    public Remove(GuildData guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Optional<GuildRepository> optRepo = guilds.guild(event).repositories().byId(event.getOption("repository", OptionMapping::getAsInt));
        if (optRepo.isEmpty()) {
            event.reply(context.localize("error.repository.unknown")).setEphemeral(true).queue();
            return;
        }
        GuildRepository repository = optRepo.get();
        if (repository.unsubscribe()) {
            event.reply(context.localize("command.repository.remove.message.removed", Replacement.create("identifier", repository.identifier()))).setEphemeral(true).queue();
        } else {
            event.reply("command.repository.remove.message.notsubscribed").setEphemeral(true).queue();
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        List<Command.Choice> choices = guilds.guild(event)
                .repositories()
                .complete(event.getFocusedOption().getValue())
                .stream()
                .map(v -> new Command.Choice(v.identifier(), v.id()))
                .toList();
        event.replyChoices(choices).queue();
    }
}
