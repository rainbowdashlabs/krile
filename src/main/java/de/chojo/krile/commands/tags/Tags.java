package de.chojo.krile.commands.tags;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.krile.commands.tags.handler.Info;
import de.chojo.krile.commands.tags.handler.List;
import de.chojo.krile.commands.tags.handler.Random;
import de.chojo.krile.data.access.GuildData;

public class Tags implements SlashProvider<Slash> {
    private final GuildData guilds;

    public Tags(GuildData guilds) {
        this.guilds = guilds;
    }

    @Override
    public Slash slash() {
        return Slash.of("tags", "Information about tags on this server")
                .subCommand(SubCommand.of("info", "Information about a tag")
                        .handler(new Info(guilds))
                        .argument(Argument.text("tag", "name of tag").withAutoComplete().asRequired()))
                .subCommand(SubCommand.of("list", "A list of all tags on this server")
                        .handler(new List(guilds)))
                .subCommand(SubCommand.of("random", "Get a random tag")
                        .handler(new Random(guilds)))
                .build();
    }
}
