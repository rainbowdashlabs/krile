package de.chojo.krile.commands.tag;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.pagination.bag.PageButton;
import de.chojo.jdautil.pagination.bag.PrivateListPageBag;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.data.access.Guilds;
import de.chojo.krile.data.dao.repository.tags.Tag;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Show implements SlashHandler {
    private final Guilds guilds;

    public Show(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Integer id = event.getOption("tag", OptionMapping::getAsInt);
        Optional<Tag> byId = guilds.guild(event.getGuild()).tags().getById(id);
        if (byId.isEmpty()) {
            event.reply("Invalid tag").setEphemeral(true).queue();
            return;
        }
        Tag tag = byId.get();
        if (tag.isPaged()) {
            context.registerPage(new PrivateListPageBag<>(tag.paged(), event.getUser().getIdLong()) {
                @Override
                public CompletableFuture<MessageEditData> buildPage() {
                    return CompletableFuture.completedFuture(MessageEditData.fromContent(currentElement()));
                }
            });
            return;
        }
        event.reply(tag.text()).queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        List<Command.Choice> choices = guilds.guild(event).tags().complete(event.getFocusedOption().getValue()).stream().limit(25).map(e -> new Command.Choice(e.name(), e.id())).toList();
        event.replyChoices(choices).queue();
    }
}
