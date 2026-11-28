/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.pagination;

import de.chojo.krile.util.Texts;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PagesTest {
    private static final Texts TEXTS = (key, entries) -> key;

    @Test
    void elementPagesRenderTheirElement() {
        Pages pages =
                Pages.ofElements(List.of("first", "second"), (element, builder, texts) -> builder.setContent(element));

        assertEquals(2, pages.count());
        assertEquals("second", render(pages, 1));
    }

    @Test
    void everyoneMayTurnUnownedPages() {
        Pages pages = Pages.of(3, (page, builder, texts) -> builder.setContent("page " + page));

        assertTrue(pages.mayTurn(1L));
        assertTrue(pages.mayTurn(2L));
    }

    @Test
    void onlyTheOwnerMayTurnOwnedPages() {
        Pages pages = Pages.of(3, (page, builder, texts) -> builder.setContent("page " + page))
                .ownedBy(1L);

        assertTrue(pages.mayTurn(1L));
        assertFalse(pages.mayTurn(2L));
        assertEquals(3, pages.count());
        assertEquals("page 2", render(pages, 2));
    }

    private static String render(Pages pages, int page) {
        MessageCreateBuilder builder = new MessageCreateBuilder();
        pages.render(page, builder, TEXTS);
        return builder.getContent();
    }
}
