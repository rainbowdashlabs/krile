package de.chojo.krile.commands.tags.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.pagination.bag.PageBag;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.data.access.GuildData;
import de.chojo.krile.data.dao.TagGuild;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class List implements SlashHandler {
    private static final int PAGE_SIZE = 10;
    private final GuildData guilds;

    public List(GuildData guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {

        TagGuild guild = guilds.guild(event);
        int pages = Math.ceilDiv(guild.tags().count(), PAGE_SIZE);
        PageBag page = new PageBag(pages) {
            @Override
            public CompletableFuture<MessageEditData> buildPage() {
                String message = guild.tags()
                        .rankingPage(current(), PAGE_SIZE)
                        .stream()
                        .map("%s"::formatted)
                        .collect(Collectors.joining("\n"));
                MessageEditData tags = MessageEditData.fromEmbeds(new EmbedBuilder()
                        .setTitle("Tags")
                        .setDescription(message)
                        .build());
                return CompletableFuture.completedFuture(tags);
            }
        };
        context.registerPage(page, true);
    }
}
