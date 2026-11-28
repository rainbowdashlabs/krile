/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.util;

import dev.goldmensch.fluava.Bundle;
import io.github.kaktushose.jdac.dispatching.events.Event;
import io.github.kaktushose.jdac.message.placeholder.Entry;

import java.util.Locale;

/**
 * Looks up the user-facing texts of the Fluent files by key, in the language of one reader.
 */
@FunctionalInterface
public interface Texts {
    /**
     * Returns the text of a key with its placeholders filled.
     *
     * @param key     key in the Fluent files
     * @param entries values of the placeholders
     * @return the text, or the key itself when no text exists for it
     */
    String get(String key, Entry... entries);

    /**
     * Texts in the language of the user who caused the event.
     *
     * @param event interaction event
     * @return texts for that user
     */
    static Texts of(Event<?> event) {
        return event::resolve;
    }

    /**
     * Texts in a given language, for code that runs outside a JDA-Commands event.
     *
     * @param bundle the default bundle of the Fluent files
     * @param locale language of the reader
     * @return texts for that language
     */
    static Texts of(Bundle bundle, Locale locale) {
        return (key, entries) -> bundle.apply(locale, key, Entry.toMap(entries));
    }
}
