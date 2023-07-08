/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.discover;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.krile.commands.discover.handler.repositories.RandomRepo;
import de.chojo.krile.commands.discover.handler.repositories.SearchRepo;
import de.chojo.krile.commands.discover.handler.tags.RandomTag;
import de.chojo.krile.commands.discover.handler.tags.SearchTag;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.data.access.TagData;

import static de.chojo.jdautil.interactions.slash.Argument.integer;
import static de.chojo.jdautil.interactions.slash.Argument.text;
import static de.chojo.jdautil.interactions.slash.Group.group;
import static de.chojo.jdautil.interactions.slash.SubCommand.sub;

public class Discover implements SlashProvider<Slash> {
    private final RepositoryData repositoryData;
    private final TagData tagData;
    private final Configuration<ConfigFile> configuration;

    public Discover(RepositoryData repositoryData, TagData tagData, Configuration<ConfigFile> configuration) {
        this.repositoryData = repositoryData;
        this.tagData = tagData;
        this.configuration = configuration;
    }

    @Override
    public Slash slash() {
        return Slash.slash("discover", "command.discover.description")
                .group(group("repositories", "command.discover.repositories.description")
                        .subCommand(sub("search", "command.discover.repositories.search.description")
                                .handler(new SearchRepo(configuration, repositoryData))
                                .argument(text("category", "command.discover.repositories.search.options.category.description").withAutoComplete())
                                .argument(text("language", "command.discover.repositories.search.options.language.description").withAutoComplete())
                                .argument(text("name", "command.discover.repositories.search.options.name.description"))
                                .argument(text("platform", "command.discover.repositories.search.options.platform.description").withAutoComplete())
                                .argument(text("user", "command.discover.repositories.search.options.user.description").withAutoComplete())
                                .argument(text("repository", "command.discover.repositories.search.options.repository.description").withAutoComplete())
                                .argument(integer("tags", "command.discover.repositories.search.options.tags.description").withAutoComplete())
                        )
                        .subCommand(sub("random", "command.discover.repositories.random.description")
                                .handler(new RandomRepo(repositoryData)))
                )
                .group(group("tags", "command.discover.tags.description")
                        .subCommand(sub("search", "command.discover.tags.search.description")
                                .handler(new SearchTag(tagData, repositoryData))
                                .argument(text("category", "command.discover.tags.search.options.category.description").withAutoComplete())
                                .argument(text("language", "command.discover.tags.search.options.language.description").withAutoComplete())
                                .argument(text("name", "command.discover.tags.search.options.name.description"))
                        )
                        .subCommand(sub("random", "command.discover.tags.random.description")
                                .handler(new RandomTag(tagData)))
                )
                .build();
    }
}
