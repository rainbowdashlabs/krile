package de.chojo.krile.commands.repositories;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.krile.commands.repositories.handler.info.Info;
import de.chojo.krile.commands.repositories.handler.info.List;
import de.chojo.krile.data.access.GuildData;

public class Repositories implements SlashProvider<Slash> {
    private final GuildData guilds;

    public Repositories(GuildData guilds) {
        this.guilds = guilds;
    }

    @Override
    public Slash slash() {
        return Slash.of("repositories", "Information about repositories on this server")
                .unlocalized()
                .subCommand(SubCommand.of("info", "Information about a repository")
                        .handler(new Info(guilds))
                        .argument(Argument.text("repository", "repository identifier").asRequired().withAutoComplete()))
                .subCommand(SubCommand.of("list", "List of repositories on this server")
                        .handler(new List(guilds)))
                .build();
    }
}
