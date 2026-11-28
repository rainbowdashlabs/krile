/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.repository;

import com.google.inject.Inject;
import de.chojo.krile.configuration.Configuration;
import de.chojo.krile.configuration.elements.RepositoryLocation;
import de.chojo.krile.data.access.GuildData;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.data.dao.Identifier;
import de.chojo.krile.data.dao.tagguild.GuildRepository;
import de.chojo.krile.service.RepoUpdateService;
import de.chojo.krile.tagimport.exception.ImportException;
import de.chojo.krile.tagimport.exception.ParsingException;
import de.chojo.logutil.marker.LogNotify;
import io.github.kaktushose.jdac.annotations.interactions.AutoComplete;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.CommandConfig;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.annotations.interactions.ReplyConfig;
import io.github.kaktushose.jdac.dispatching.events.interactions.AutoCompleteEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Manages the repositories of a guild. Only administrators see the command unless the guild grants it to others.
 */
@Interaction("repository")
@CommandConfig(enabledFor = Permission.ADMINISTRATOR, context = InteractionContextType.GUILD)
@ReplyConfig(ephemeral = true)
public class Repository {
    private static final Logger log = getLogger(Repository.class);
    private final RepositoryData repositoryData;
    private final GuildData guilds;
    private final Configuration configuration;
    private final RepoUpdateService updateService;

    @Inject
    public Repository(
            RepositoryData repositoryData,
            GuildData guilds,
            Configuration configuration,
            RepoUpdateService updateService) {
        this.repositoryData = repositoryData;
        this.guilds = guilds;
        this.configuration = configuration;
        this.updateService = updateService;
    }

    @Command(value = "add url", desc = "Use a URL to add a new repository")
    public void addUrl(CommandEvent event, @Param(name = "url", value = "repository URL") String url) {
        Optional<RepositoryLocation> location =
                configuration.main().repositories().find(url);
        if (location.isEmpty()) {
            event.reply("error-url-invalidsource");
            return;
        }
        add(event, location.get().extractIdentifier(url));
    }

    @Command(value = "add identifier", desc = "Use an identifier to add a new repository")
    public void addIdentifier(
            CommandEvent event, @Param(name = "identifier", value = "repository identifier") String identifier) {
        Optional<Identifier> parsed = Identifier.parse(identifier);
        if (parsed.isEmpty()) {
            event.reply("error-identifier-invalid");
            return;
        }
        if (configuration.main().repositories().find(parsed.get()).isEmpty()) {
            event.reply("error-source-invalid");
            return;
        }
        add(event, parsed.get());
    }

    @AutoComplete(value = "addIdentifier", options = "identifier")
    public void completeIdentifier(AutoCompleteEvent event) {
        RepositoryCompletion.completeStrings(event, repositoryData.completeIdentifier(event.getValue()));
    }

    @Command(value = "add name", desc = "Use a name to add a new repository")
    public void addName(
            CommandEvent event,
            @Param(name = "platform", value = "platform name") String platform,
            @Param(name = "user", value = "user name") String user,
            @Param(name = "repository", value = "repository name") String repository,
            @Param(name = "path", value = "repository path", optional = true) String path) {
        Identifier identifier = Identifier.of(platform, user, repository, path);
        if (configuration.main().repositories().find(identifier).isEmpty()) {
            event.reply("error-source-invalid");
            return;
        }
        add(event, identifier);
    }

    @AutoComplete(value = "addName", options = "platform")
    public void completePlatform(AutoCompleteEvent event) {
        RepositoryCompletion.completePlatform(event, configuration);
    }

    @AutoComplete(value = "addName", options = "user")
    public void completeUser(AutoCompleteEvent event) {
        RepositoryCompletion.completeStrings(event, repositoryData.completeUser(event.getValue()));
    }

    @AutoComplete(value = "addName", options = "repository")
    public void completeRepositoryName(AutoCompleteEvent event) {
        RepositoryCompletion.completeStrings(event, repositoryData.completeRepository(event.getValue()));
    }

    @AutoComplete(value = "addName", options = "path")
    public void completePath(AutoCompleteEvent event) {
        RepositoryCompletion.completeStrings(event, repositoryData.completePath(event.getValue()));
    }

    @Command(value = "remove", desc = "Remove a repository")
    public void remove(
            CommandEvent event, @Param(name = "repository", value = "repository identifier") String repository) {
        Optional<GuildRepository> resolved = RepositoryCompletion.resolve(guilds.guild(event.getGuild()), repository);
        if (resolved.isEmpty()) {
            event.reply("error-repository-unknown");
            return;
        }
        if (resolved.get().unsubscribe()) {
            event.reply(
                    "command-repository-remove-message-removed",
                    entry("identifier", resolved.get().identifier().toString()));
        } else {
            event.reply("command-repository-remove-message-notsubscribed");
        }
    }

    @Command(value = "update", desc = "Manually update a repository")
    public void update(
            CommandEvent event, @Param(name = "repository", value = "repository identifier") String repository) {
        Optional<GuildRepository> resolved = RepositoryCompletion.resolve(guilds.guild(event.getGuild()), repository);
        if (resolved.isEmpty()) {
            event.reply("error-repository-unknown");
            return;
        }
        Instant checked = resolved.get().data().get().checked();
        int minCheck = configuration.main().repositories().minCheck();
        if (checked.isAfter(Instant.now().minus(minCheck, ChronoUnit.MINUTES))) {
            event.reply(
                    "command-repository-update-message-cooldown",
                    entry("minutes", minCheck),
                    entry("relative", TimeFormat.RELATIVE.format(checked)));
            return;
        }
        event.reply("command-repository-update-message-scheduled");
        updateService.schedule(resolved.get());
    }

    @AutoComplete(
            value = {"remove", "update"},
            options = "repository")
    public void completeGuildRepository(AutoCompleteEvent event) {
        RepositoryCompletion.complete(event, guilds.guild(event.getGuild()));
    }

    private void add(CommandEvent event, Identifier identifier) {
        event.reply("command-add-message-resolving");
        Optional<de.chojo.krile.data.dao.Repository> repository;
        try {
            repository = repositoryData.getOrCreateByIdentifier(identifier, event::reply);
        } catch (ParsingException e) {
            log.error("Error while parsing", e);
            event.reply("error-repository-parsing", entry("error", e.getMessage()));
            return;
        } catch (ImportException e) {
            log.error(LogNotify.NOTIFY_ADMIN, "Could not import repository", e);
            event.reply("error-repository-import");
            return;
        }
        guilds.guild(event.getGuild()).repositories().add(repository.get());
        event.reply("command-add-message-added");
    }
}
