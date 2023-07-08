/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.tags;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.krile.commands.tags.handler.Info;
import de.chojo.krile.commands.tags.handler.List;
import de.chojo.krile.commands.tags.handler.Random;
import de.chojo.krile.data.access.GuildData;

import static de.chojo.jdautil.interactions.slash.SubCommand.sub;

public class Tags implements SlashProvider<Slash> {
    private final GuildData guilds;

    public Tags(GuildData guilds) {
        this.guilds = guilds;
    }

    @Override
    public Slash slash() {
        return Slash.of("tags", "command.tags.description")
                .subCommand(sub("info", "command.tags.info.description")
                        .handler(new Info(guilds))
                        .argument(Argument.text("tag", "command.tags.info.options.tag.description").withAutoComplete().asRequired()))
                .subCommand(sub("list", "command.tags.list.description")
                        .handler(new List(guilds)))
                .subCommand(sub("random", "command.tags.random.description")
                        .handler(new Random(guilds)))
                .build();
    }
}
