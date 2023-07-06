package de.chojo.krile.commands.repositories.handler.remove;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.data.access.Guilds;
import de.chojo.krile.data.dao.tagguild.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;
import java.util.Optional;

public class Remove implements SlashHandler {
    private final Guilds guilds;

    public Remove(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Optional<GuildRepository> optRepo = guilds.guild(event).repositories().byId(event.getOption("repository", OptionMapping::getAsInt));
        if (optRepo.isEmpty()) {
            event.reply("Unknown repository").setEphemeral(true).queue();
            return;
        }
        GuildRepository repository = optRepo.get();
        if (repository.unsubscribe()) {
            event.reply("Repository `%s` removed from guild".formatted(repository.identifier())).setEphemeral(true).queue();
        } else {
            event.reply("Could not remove repository").setEphemeral(true).queue();
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
        SlashHandler.super.onAutoComplete(event, context);
    }
}
