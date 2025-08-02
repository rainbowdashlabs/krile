/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.repository.handler.add;

import de.chojo.jdautil.configuration.Configuration;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.data.access.GuildData;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.data.dao.Identifier;
import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.tagimport.exception.ImportException;
import de.chojo.krile.tagimport.exception.ParsingException;
import de.chojo.logutil.marker.LogNotify;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class BaseAdd implements SlashHandler {
    private static final Logger log = getLogger(BaseAdd.class);
    private final RepositoryData repositoryData;
    private final GuildData guilds;
    private final Configuration<ConfigFile> configuration;

    public BaseAdd(RepositoryData repositoryData, GuildData guilds, Configuration<ConfigFile> configuration) {
        this.repositoryData = repositoryData;
        this.guilds = guilds;
        this.configuration = configuration;
    }

    public void add(SlashCommandInteractionEvent event, EventContext context, Identifier identifier) {
        event.reply(context.guildLocale("command.add.message.resolving")).setEphemeral(true).queue();
        Optional<Repository> repository;
        try {
            repository = repositoryData.getOrCreateByIdentifier(identifier, context, event);
        } catch (ParsingException e) {
            event.getHook().editOriginal(context.guildLocale("error.repository.parsing", Replacement.create("error", e.getMessage()))).queue();
            log.error("Error while parsing", e);
            return;
        } catch (ImportException e) {
            log.error(LogNotify.NOTIFY_ADMIN, "Could not import repository", e);
            event.getHook().editOriginal(context.guildLocale("error.repository.import")).queue();
            return;
        }
        guilds.guild(event.getGuild()).repositories().add(repository.get());
        event.getHook().editOriginal(context.localize("command.add.message.added")).queue();
    }

    public Configuration<ConfigFile> configuration() {
        return configuration;
    }

    public RepositoryData repositoryData() {
        return repositoryData;
    }
}
