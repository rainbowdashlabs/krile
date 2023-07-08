/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.tag;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.krile.commands.tag.handler.Show;
import de.chojo.krile.data.access.GuildData;

import static de.chojo.jdautil.interactions.slash.Argument.text;

public class Tag implements SlashProvider<Slash> {
    private final GuildData guilds;

    public Tag(GuildData guilds) {
        this.guilds = guilds;
    }

    @Override
    public Slash slash() {
        return Slash.of("tag", "command.tag.description")
                .command(new Show(guilds))
                .argument(text("tag", "command.tag.options.tag.description").withAutoComplete().asRequired())
                .build();
    }
}
