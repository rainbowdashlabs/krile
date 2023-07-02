package de.chojo.krile.commands.repositories.handler.add;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.jdautil.container.Pair;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.configuration.elements.RepositoryLocation;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.data.dao.RepositoryUpdateException;
import de.chojo.krile.tagimport.repo.RawTagRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.Optional;

public class Url implements SlashHandler {
    private final RepositoryData repositoryData;
    private final Configuration<ConfigFile> configuration;

    public Url(RepositoryData repositoryData, Configuration<ConfigFile> configuration) {
        this.repositoryData = repositoryData;
        this.configuration = configuration;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        String url = event.getOption("url", OptionMapping::getAsString);
        Optional<RepositoryLocation> optLocation = configuration.config().repositories().find(url);
        if (optLocation.isEmpty()) {
            event.reply("Invalid source url").setEphemeral(true).queue();
            return;
        }
        Optional<Repository> repository = repositoryData.byUrl(url);
        if (repository.isEmpty()) {
            event.reply("Unknown repo. Starting import.").setEphemeral(true).queue();
            Pair<String, String> userRepoPair = optLocation.get().parseUrl(url);
            try {
                RawTagRepository rawTagRepository = RawTagRepository.create(optLocation.get(), userRepoPair.first, userRepoPair.second);
                Optional<Repository> newRepo = repositoryData.create(rawTagRepository);
                if (newRepo.isEmpty()) {
                    event.getHook().editOriginal("Could not create new repository").queue();
                    return;
                }
                newRepo.get().update(rawTagRepository);
                event.getHook().editOriginal("Repository created and updated.").queue();
            } catch (IOException | GitAPIException | RepositoryUpdateException e) {
                event.getHook().editOriginal("Failed to parse repository.").queue();
            }
            return;
        }
        // TODO Add to repositories
        event.reply("Repository added to server").setEphemeral(true).queue();
    }
}
