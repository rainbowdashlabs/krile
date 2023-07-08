/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.repository;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.jdautil.interactions.slash.Group;
import de.chojo.jdautil.interactions.slash.Slash;
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

import static de.chojo.jdautil.interactions.slash.Argument.text;
import static de.chojo.jdautil.interactions.slash.SubCommand.sub;

public class Repository extends SlashCommand {
    public Repository(RepositoryData repositoryData, GuildData guilds, Configuration<ConfigFile> configuration, RepoUpdateService updateService) {
        super(Slash.of("repository", "command.repository.description")
                .adminCommand()
                .group(Group.of("add", "command.repository.add.description")
                        .subCommand(sub("url", "command.repository.add.url.description")
                                .handler(new ByUrl(repositoryData, guilds, configuration))
                                .argument(text("url", "command.repository.add.url.options.url.description").asRequired())
                        )
                        .subCommand(sub("identifier", "command.repository.add.identifier.description")
                                .handler(new ByIdentifier(repositoryData, guilds, configuration))
                                .argument(text("identifier", "command.repository.add.identifier.options.identifier.description").asRequired().withAutoComplete())
                        )
                        .subCommand(sub("name", "command.repository.add.name.description")
                                .handler(new ByNames(repositoryData, guilds, configuration))
                                .argument(text("platform", "command.repository.add.name.options.platform.description").asRequired().withAutoComplete())
                                .argument(text("user", "command.repository.add.name.options.user.description").asRequired().withAutoComplete())
                                .argument(text("repository", "command.repository.add.name.options.repository.description").asRequired().withAutoComplete())
                                .argument(text("path", "command.repository.add.name.options.path.description").withAutoComplete())
                        )
                )
                .subCommand(sub("remove", "command.repository.remove.description")
                        .handler(new Remove(guilds))
                        .argument(text("repository", "command.repository.remove.options.repository.description").asRequired().withAutoComplete()))
                .subCommand(sub("update", "command.repository.update.description")
                        .handler(new Update(guilds, updateService, configuration))
                        .argument(text("repository", "command.repository.update.options.repository.description").asRequired().withAutoComplete()))
                .build());
    }
}
