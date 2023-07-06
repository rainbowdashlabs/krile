/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.tag;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.krile.commands.tag.handler.Show;
import de.chojo.krile.data.access.GuildData;

public class Tag implements SlashProvider<Slash> {
    private final GuildData guilds;

    public Tag(GuildData guilds) {
        this.guilds = guilds;
    }

    @Override
    public Slash slash() {
        return Slash.of("tag", "Retrieve a tag")
                .unlocalized()
                .command(new Show(guilds))
                .argument(Argument.text("tag", "Get a tag").withAutoComplete().asRequired())
                .build();
    }
}
