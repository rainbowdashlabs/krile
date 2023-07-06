package de.chojo.krile.commands.discover;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Group;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;

public class Discover implements SlashProvider<Slash> {
    @Override
    public Slash slash() {
        return Slash.of("discover", "Discover new tags and repositories")
                .unlocalized()
                .group(Group.of("repositories", "Discover new repositories")
                        .subCommand(SubCommand.of("search", "Search repositories by filters")
                                .handler(null)
                                .argument(Argument.text("category", "category").withAutoComplete())
                                .argument(Argument.text("language", "language of the repository containing the tag").withAutoComplete())
                                .argument(Argument.text("platform", "platform").withAutoComplete())
                                .argument(Argument.text("user", "username").withAutoComplete())
                                .argument(Argument.text("repo", "repository name").withAutoComplete())
                                .argument(Argument.integer("tags", "Min amount of tags associated with this repository").withAutoComplete())

                        )
                        .subCommand(SubCommand.of("random", "Get a random repository")
                                .handler(null))
                )
                .group(Group.of("tags", "Discover new tags")
                        .subCommand(SubCommand.of("search", "Search a tag by filters")
                                .handler(null)
                                .argument(Argument.text("category", "category").withAutoComplete())
                                .argument(Argument.text("language", "language of the repository containing the tag").withAutoComplete())
                                .argument(Argument.text("name", "part of the name"))
                        )
                        .subCommand(SubCommand.of("random", "Get a random tag from any repository")
                                .handler(null))
                )
                .build();
    }
}
