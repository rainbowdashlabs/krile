/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.pagination;

import de.chojo.krile.util.Texts;
import io.github.kaktushose.jdac.annotations.interactions.Button;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ComponentEvent;
import io.github.kaktushose.jdac.dispatching.reply.Component;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;

import java.util.List;

/**
 * Shows {@link Pages} one at a time, with buttons to the previous and the next page.
 * <p>
 * The pages and the current position are kept in the conversation of the reply, so paging works as long as the
 * conversation lives and ends with it.
 */
@Interaction
public class Pagination {
    private static final String STATE = "pagination";

    /**
     * Replies with the first page.
     *
     * @param event     event to reply to
     * @param pages     pages to show, at least one
     * @param ephemeral whether only the user sees the reply
     * @param buttons   further buttons shown in their own row, may be empty
     */
    public static void reply(
            ReplyableEvent<?> event, Pages pages, boolean ephemeral, List<? extends ActionRowChildComponent> buttons) {
        event.keyValueStore().put(STATE, new State(pages, 0));
        Texts texts = Texts.of(event);
        event.with()
                .ephemeral(ephemeral)
                .builder(builder -> {
                    pages.render(0, builder, texts);
                    if (!buttons.isEmpty()) builder.addComponents(ActionRow.of(buttons));
                })
                .components(Component.button(Pagination.class, "previous"), Component.button(Pagination.class, "next"))
                .reply();
    }

    @Button(emoji = "◀️", style = ButtonStyle.SECONDARY)
    public void previous(ComponentEvent event) {
        move(event, -1);
    }

    @Button(emoji = "▶️", style = ButtonStyle.SECONDARY)
    public void next(ComponentEvent event) {
        move(event, 1);
    }

    private void move(ComponentEvent event, int step) {
        State state = event.keyValueStore().getOrThrow(STATE, State.class);
        if (!state.pages().mayTurn(event.getUser().getIdLong())) {
            event.with().editReply(false).keepComponents(false).ephemeral(true).reply("❌");
            return;
        }
        int page = Math.floorMod(state.current() + step, state.pages().count());
        event.keyValueStore().put(STATE, new State(state.pages(), page));
        Texts texts = Texts.of(event);
        event.with()
                .keepComponents(true)
                .builder(builder -> state.pages().render(page, builder, texts))
                .reply();
    }

    private record State(Pages pages, int current) {}
}
