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
        return Slash.of("preview", "command.preview.description")
                .subCommand(SubCommand.of("modal", "command.preview.modal.description")
                        .handler(new ByModal()))
                .subCommand(SubCommand.of("url", "command.preview.url.description")
                        .handler(new ByUrl())
                        .argument(Argument.text("url", "command.preview.url.options.url.description").asRequired()))
                .build();
    }
}
