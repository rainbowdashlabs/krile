/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.pagination;

import de.chojo.krile.util.Texts;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.List;

/**
 * The pages of a reply shown by {@link Pagination}.
 */
public interface Pages {
    /**
     * Number of pages.
     *
     * @return the page count, at least one
     */
    int count();

    /**
     * Writes the content of a page into a message.
     *
     * @param page    index of the page, starting at zero
     * @param builder message to write into
     * @param texts   texts in the language of the user who turns the page
     */
    void render(int page, MessageCreateBuilder builder, Texts texts);

    /**
     * Whether a user may turn the pages.
     *
     * @param userId id of the user who clicked
     * @return true when the user may turn the pages
     */
    default boolean mayTurn(long userId) {
        return true;
    }

    /**
     * Pages that only one user may turn.
     *
     * @param owner the only user who may turn the pages
     * @return the restricted pages
     */
    default Pages ownedBy(long owner) {
        Pages pages = this;
        return new Pages() {
            @Override
            public int count() {
                return pages.count();
            }

            @Override
            public void render(int page, MessageCreateBuilder builder, Texts texts) {
                pages.render(page, builder, texts);
            }

            @Override
            public boolean mayTurn(long userId) {
                return userId == owner;
            }
        };
    }

    /**
     * Pages with a fixed count whose content comes from a function.
     *
     * @param count  number of pages
     * @param render writes a page into a message
     * @return the pages
     */
    static Pages of(int count, PageRenderer render) {
        return new Pages() {
            @Override
            public int count() {
                return count;
            }

            @Override
            public void render(int page, MessageCreateBuilder builder, Texts texts) {
                render.render(page, builder, texts);
            }
        };
    }

    /**
     * Pages with one element of a list on each page.
     *
     * @param elements elements, at least one
     * @param render   writes an element into a message
     * @param <T>      type of the elements
     * @return the pages
     */
    static <T> Pages ofElements(List<T> elements, ElementRenderer<T> render) {
        return of(elements.size(), (page, builder, texts) -> render.render(elements.get(page), builder, texts));
    }

    /**
     * Writes a page into a message.
     */
    @FunctionalInterface
    interface PageRenderer {
        void render(int page, MessageCreateBuilder builder, Texts texts);
    }

    /**
     * Writes one element into a message.
     *
     * @param <T> type of the element
     */
    @FunctionalInterface
    interface ElementRenderer<T> {
        void render(T element, MessageCreateBuilder builder, Texts texts);
    }
}
