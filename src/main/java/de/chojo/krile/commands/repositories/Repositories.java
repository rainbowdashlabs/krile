/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.repositories;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.krile.commands.repositories.handler.info.Info;
import de.chojo.krile.commands.repositories.handler.info.List;
import de.chojo.krile.data.access.GuildData;

import static de.chojo.jdautil.interactions.slash.Argument.text;
import static de.chojo.jdautil.interactions.slash.SubCommand.sub;

public class Repositories implements SlashProvider<Slash> {
    private final GuildData guilds;

    public Repositories(GuildData guilds) {
        this.guilds = guilds;
    }

    @Override
    public Slash slash() {
        return Slash.of("repositories", "command.repositories.description")
                .subCommand(sub("info", "command.repositories.info.description")
                        .handler(new Info(guilds))
                        .argument(text("repository", "command.repositories.info.options.repository.description").asRequired().withAutoComplete()))
                .subCommand(sub("list", "command.repositories.list.description")
                        .handler(new List(guilds)))
                .build();
    }
}
