package de.chojo.krile.commands.discover;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Group;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.krile.commands.discover.handler.repositories.RandomRepo;
import de.chojo.krile.commands.discover.handler.repositories.SearchRepo;
import de.chojo.krile.commands.discover.handler.tags.RandomTag;
import de.chojo.krile.commands.discover.handler.tags.SearchTag;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.data.access.TagData;

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
        return Slash.of("discover", "Discover new tags and repositories")
                .unlocalized()
                .group(Group.of("repositories", "Discover new repositories")
                        .subCommand(SubCommand.of("search", "Search repositories by filters")
                                .handler(new SearchRepo(configuration, repositoryData))
                                .argument(Argument.text("category", "category").withAutoComplete())
                                .argument(Argument.text("language", "language of the repository containing the tag").withAutoComplete())
                                .argument(Argument.text("name", "name of the repository"))
                                .argument(Argument.text("platform", "platform").withAutoComplete())
                                .argument(Argument.text("user", "username").withAutoComplete())
                                .argument(Argument.text("repo", "repository name").withAutoComplete())
                                .argument(Argument.integer("tags", "Min amount of tags associated with this repository").withAutoComplete())
                        )
                        .subCommand(SubCommand.of("random", "Get a random repository")
                                .handler(new RandomRepo(repositoryData)))
                )
                .group(Group.of("tags", "Discover new tags")
                        .subCommand(SubCommand.of("search", "Search a tag by filters")
                                .handler(new SearchTag(tagData, repositoryData))
                                .argument(Argument.text("category", "category").withAutoComplete())
                                .argument(Argument.text("language", "language of the repository containing the tag").withAutoComplete())
                                .argument(Argument.text("name", "part of the name"))
                        )
                        .subCommand(SubCommand.of("random", "Get a random tag from any repository")
                                .handler(new RandomTag(tagData)))
                )
                .build();
    }
}
