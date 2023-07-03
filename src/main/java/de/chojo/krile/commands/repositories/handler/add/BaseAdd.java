package de.chojo.krile.commands.repositories.handler.add;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.data.dao.Identifier;
import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.data.dao.RepositoryUpdateException;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.Optional;

public abstract class BaseAdd implements SlashHandler {
    private final RepositoryData repositoryData;
    private final Configuration<ConfigFile> configuration;

    public BaseAdd(RepositoryData repositoryData, Configuration<ConfigFile> configuration) {
        this.repositoryData = repositoryData;
        this.configuration = configuration;
    }

    public void add(SlashCommandInteractionEvent event, EventContext context, Identifier identifier) {
        event.reply("Resolving repository").setEphemeral(true).queue();
        Optional<Repository> repository;
        try {
            repository = repositoryData.getOrCreateByIdentifier(identifier, event);
        } catch (GitAPIException | IOException | RepositoryUpdateException e) {
            event.getHook().editOriginal("Failed to parse repository.").queue();
            return;
        }
        // TODO Add to repositories
        event.reply("Repository added to server").setEphemeral(true).queue();
    }

    public Configuration<ConfigFile> configuration() {
        return configuration;
    }
}
