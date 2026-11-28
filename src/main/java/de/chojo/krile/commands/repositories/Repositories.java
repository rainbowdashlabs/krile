/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.repositories;

import com.google.inject.Inject;
import de.chojo.krile.commands.pagination.Pages;
import de.chojo.krile.commands.pagination.Pagination;
import de.chojo.krile.commands.repository.RepositoryCompletion;
import de.chojo.krile.data.access.GuildData;
import de.chojo.krile.data.dao.tagguild.GuildRepository;
import de.chojo.krile.util.Texts;
import io.github.kaktushose.jdac.annotations.interactions.AutoComplete;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.interactions.AutoCompleteEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Information about the repositories a guild uses.
 */
@Interaction("repositories")
public class Repositories {
    private static final int PAGE_SIZE = 10;
    private final GuildData guilds;

    @Inject
    public Repositories(GuildData guilds) {
        this.guilds = guilds;
    }

    @Command(value = "info", desc = "Information about a repository")
    public void info(
            CommandEvent event, @Param(name = "repository", value = "repository identifier") String repository) {
        Optional<GuildRepository> resolved = RepositoryCompletion.resolve(guilds.guild(event.getGuild()), repository);
        if (resolved.isEmpty()) {
            event.with().ephemeral(true).reply("error-repository-unknown");
            return;
        }
        event.with()
                .ephemeral(true)
                .builder(message -> message.setEmbeds(resolved.get().infoEmbed(Texts.of(event))))
                .reply();
    }

    @AutoComplete(value = "info", options = "repository")
    public void completeRepository(AutoCompleteEvent event) {
        RepositoryCompletion.complete(event, guilds.guild(event.getGuild()));
    }

    @Command(value = "list", desc = "List of repositories on this server")
    public void list(CommandEvent event) {
        var repositories = guilds.guild(event.getGuild()).repositories();
        int pages = Math.max(1, Math.ceilDiv(repositories.count(), PAGE_SIZE));
        Pagination.reply(
                event,
                Pages.of(pages, (page, builder, texts) -> {
                    String list = repositories.page(page, PAGE_SIZE).stream()
                            .map("%s"::formatted)
                            .collect(Collectors.joining("\n"));
                    builder.setEmbeds(new EmbedBuilder()
                            .setTitle(texts.get("words-repositories"))
                            .setDescription(list)
                            .build());
                }),
                true,
                List.of());
    }
}
