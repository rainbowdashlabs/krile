/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.repositories.handler.info;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.pagination.bag.PageBag;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.data.access.GuildData;
import de.chojo.krile.data.dao.tagguild.Repositories;
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
        Repositories repositories = guilds.guild(event).repositories();
        int pages = Math.ceilDiv(repositories.count(), PAGE_SIZE);
        PageBag page = new PageBag(pages) {
            @Override
            public CompletableFuture<MessageEditData> buildPage() {
                String message = repositories.page(current(), PAGE_SIZE)
                        .stream()
                        .map("%s"::formatted)
                        .collect(Collectors.joining("\n"));
                MessageEditData tags = MessageEditData.fromEmbeds(new EmbedBuilder()
                        .setTitle("Repositories")
                        .setDescription(message)
                        .build());
                return CompletableFuture.completedFuture(tags);
            }
        };
        context.registerPage(page, true);

    }
}
