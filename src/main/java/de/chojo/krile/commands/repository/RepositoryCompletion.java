/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.repository;

import de.chojo.krile.configuration.Configuration;
import de.chojo.krile.configuration.elements.RepositoryLocation;
import de.chojo.krile.data.dao.TagGuild;
import de.chojo.krile.data.dao.tagguild.GuildRepository;
import de.chojo.krile.util.Numbers;
import dev.chojo.aether.common.completion.Choice;
import dev.chojo.aether.common.completion.Completion;
import io.github.kaktushose.jdac.dispatching.events.interactions.AutoCompleteEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;
import java.util.Optional;

/**
 * Completes and resolves the repositories a guild uses. The completion offers the identifier as name and the
 * repository id as value, so the command option receives the id.
 */
public final class RepositoryCompletion {
    private RepositoryCompletion() {}

    /**
     * Replies with the guild's repositories matching the typed value.
     *
     * @param event autocomplete event
     * @param guild guild whose repositories are offered
     */
    public static void complete(AutoCompleteEvent event, TagGuild guild) {
        event.replyChoices(guild.repositories().complete(event.getValue()).stream()
                .map(repository -> new Command.Choice(repository.identifier(), repository.id()))
                .toList());
    }

    /**
     * Resolves the value of a repository option, which is the repository id chosen from the completion.
     *
     * @param guild guild whose repositories are searched
     * @param value option value
     * @return the repository, or empty when the value is no id of a repository of the guild
     */
    public static Optional<GuildRepository> resolve(TagGuild guild, String value) {
        return Numbers.parseInt(value.trim()).flatMap(id -> guild.repositories().byId(id));
    }

    /**
     * Replies with the names of the configured repository platforms that start with the typed value.
     *
     * @param event         autocomplete event
     * @param configuration configuration holding the platforms
     */
    public static void completePlatform(AutoCompleteEvent event, Configuration configuration) {
        event.replyChoices(Completion.complete(
                event.getValue(), configuration.main().repositories().repositories(), RepositoryLocation::name));
    }

    /**
     * Replies with the values matching the typed value, each used as name and value.
     *
     * @param event  autocomplete event
     * @param values values to offer
     */
    public static void completeStrings(AutoCompleteEvent event, List<String> values) {
        event.replyChoices(Choice.toStringChoice(values.stream().limit(25)));
    }
}
