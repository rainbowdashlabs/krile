package de.chojo.krile.commands.preview;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.krile.commands.preview.handler.ByModal;
import de.chojo.krile.commands.preview.handler.ByUrl;

public class Preview implements SlashProvider<Slash> {
    @Override
    public Slash slash() {
        return Slash.of("preview", "Preview a tag")
                .unlocalized()
                .subCommand(SubCommand.of("modal", "Create a preview by using a modal as input")
                        .handler(new ByModal()))
                .subCommand(SubCommand.of("url", "Use a file from an url")
                        .handler(new ByUrl())
                        .argument(Argument.text("url", "Url to download").asRequired()))
                .build();
    }
}
