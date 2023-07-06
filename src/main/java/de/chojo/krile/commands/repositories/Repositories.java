package de.chojo.krile.commands.repositories;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Group;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.krile.commands.repositories.handler.add.ByIdentifier;
import de.chojo.krile.commands.repositories.handler.add.ByNames;
import de.chojo.krile.commands.repositories.handler.add.ByUrl;
import de.chojo.krile.commands.repositories.handler.remove.Remove;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.data.access.Guilds;
import de.chojo.krile.data.access.RepositoryData;

public class Repositories extends SlashCommand {
    public Repositories(RepositoryData repositoryData, Guilds guilds, Configuration<ConfigFile> configuration) {
        super(Slash.of("repository", "Manage repositories of your guild")
                .unlocalized()
                .adminCommand()
                .group(Group.of("add", "add a repository")
                        .subCommand(SubCommand.of("url", "Use a url to add a new repository")
                                .handler(new ByUrl(repositoryData, guilds, configuration))
                                .argument(Argument.text("url", "repository url").asRequired()))
                        .subCommand(SubCommand.of("identifier", "Use an identifier to add a new repository")
                                .handler(new ByIdentifier(repositoryData, guilds, configuration))
                                .argument(Argument.text("identifier", "repository identifier").asRequired().withAutoComplete())
                        )
                        .subCommand(SubCommand.of("name", "Use a url to add a new repository")
                                .handler(new ByNames(repositoryData, guilds, configuration))
                                .argument(Argument.text("platform", "platform name").asRequired().withAutoComplete())
                                .argument(Argument.text("user", "user name").asRequired().withAutoComplete())
                                .argument(Argument.text("repository", "repository name").asRequired().withAutoComplete())
                                .argument(Argument.text("path", "repository path").withAutoComplete())
                        )
                )
                .subCommand(SubCommand.of("remove", "Remove a repository")
                        .handler(new Remove(guilds))
                        .argument(Argument.text("repository", "repository identifier").asRequired().withAutoComplete()))
                .build());
    }
}
