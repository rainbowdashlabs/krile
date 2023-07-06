package de.chojo.krile.commands.tags.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.data.access.GuildData;
import de.chojo.krile.data.dao.repository.tags.Tag;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;
import java.util.Optional;

public class Info implements SlashHandler {
    private final GuildData guilds;

    public Info(GuildData guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Optional<Tag> byId = guilds.guild(event).tags().getById(event.getOption("tag", -1, OptionMapping::getAsInt));
        if (byId.isEmpty()) {
            event.reply("Invalid tag").setEphemeral(true).queue();
            return;
        }
        MessageEmbed messageEmbed = byId.get().infoEmbed(context);
        event.replyEmbeds(messageEmbed).setEphemeral(true).queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        List<Command.Choice> choices = guilds.guild(event).tags()
                .complete(event.getFocusedOption().getValue())
                .stream()
                .map(e -> new Command.Choice(e.name(), e.id()))
                .toList();
        event.replyChoices(choices).queue();
    }
}
