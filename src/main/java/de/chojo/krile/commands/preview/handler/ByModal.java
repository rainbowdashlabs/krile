package de.chojo.krile.commands.preview.handler;

import de.chojo.jdautil.modals.handler.ModalHandler;
import de.chojo.jdautil.modals.handler.TextInputHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public class ByModal extends PreviewBase {
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        context.registerModal(ModalHandler.builder("command.preview.modal.modal.label")
                .addInput(TextInputHandler.builder("tag", "command.preview.modal.modal.field.label", TextInputStyle.PARAGRAPH))
                .withHandler(e -> {
                    String text = e.getValue("tag").getAsString();
                    showTag(text, e, context);
                })
                .build());
    }
}
