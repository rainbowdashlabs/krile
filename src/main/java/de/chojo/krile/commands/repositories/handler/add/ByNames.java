package de.chojo.krile.commands.repositories.handler.add;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.configuration.elements.RepositoryLocation;
import de.chojo.krile.data.access.Guilds;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.data.dao.Identifier;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Optional;

public class ByNames extends BaseAdd {

    public ByNames(RepositoryData repositoryData, Guilds guilds, Configuration<ConfigFile> configuration) {
        super(repositoryData, guilds, configuration);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        String platform = event.getOption("platform", OptionMapping::getAsString);
        String name = event.getOption("name", OptionMapping::getAsString);
        String repo = event.getOption("repo", OptionMapping::getAsString);
        String path = event.getOption("path", OptionMapping::getAsString);
        Identifier identifier = Identifier.of(platform, name, repo, path);
        Optional<RepositoryLocation> optLocation = configuration().config().repositories().find(identifier);
        if (optLocation.isEmpty()) {
            event.reply("Invalid source").setEphemeral(true).queue();
            return;
        }
        add(event, context, identifier);
    }
}
