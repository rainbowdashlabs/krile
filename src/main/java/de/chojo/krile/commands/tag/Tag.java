package de.chojo.krile.commands.tag;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.krile.commands.tag.handler.Show;
import de.chojo.krile.data.access.Guilds;

public class Tag implements SlashProvider<Slash> {
    private final Guilds guilds;

    public Tag(Guilds guilds) {
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
