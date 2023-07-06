/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.repository;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Group;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.krile.commands.repository.handler.add.ByIdentifier;
import de.chojo.krile.commands.repository.handler.add.ByNames;
import de.chojo.krile.commands.repository.handler.add.ByUrl;
import de.chojo.krile.commands.repository.handler.remove.Remove;
import de.chojo.krile.commands.repository.handler.update.Update;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.data.access.GuildData;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.service.RepoUpdateService;

public class Repository extends SlashCommand {
    public Repository(RepositoryData repositoryData, GuildData guilds, Configuration<ConfigFile> configuration, RepoUpdateService updateService) {
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
                .subCommand(SubCommand.of("update", "Manually update a repository")
                        .handler(new Update(guilds, updateService, configuration))
                        .argument(Argument.text("repository", "repository identifier").asRequired().withAutoComplete()))
                .build());
    }
}
