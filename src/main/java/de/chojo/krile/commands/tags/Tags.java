package de.chojo.krile.commands.tags;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;

public class Tags implements SlashProvider<Slash> {
    @Override
    public Slash slash() {
        return Slash.of("tags", "Information about tags on this server")
                .subCommand(SubCommand.of("info", "Information about a tag")
                        .handler(null)
                        .argument(Argument.text("tag", "name of tag").withAutoComplete().asRequired()))
                .subCommand(SubCommand.of("list", "A list of all tags on this server")
                        .handler(null))
                .subCommand(SubCommand.of("random", "Get a random tag")
                        .handler(null))
                .build();
    }
}
