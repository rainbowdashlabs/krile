package de.chojo.krile.commands.repositories;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Group;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.krile.commands.repositories.handler.add.ByUrl;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.data.access.RepositoryData;

public class Repositories extends SlashCommand {
    public Repositories(RepositoryData repositoryData, Configuration<ConfigFile> configuration) {
        super(Slash.of("repository", "Manage repositories of your guild")
                .unlocalized()
                .group(Group.of("add", "add a repository")
                        .subCommand(SubCommand.of("url", "Use a url to add a new repository")
                                .handler(new ByUrl(repositoryData, configuration))
                                .argument(Argument.text("url", "repository url").asRequired()))
                )
                .build());
    }
}
