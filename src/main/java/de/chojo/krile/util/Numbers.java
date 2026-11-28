/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.util;

import java.util.Optional;

/**
 * Parses numbers typed by users.
 */
public final class Numbers {
    private Numbers() {}

    /**
     * Parses a whole number.
     *
     * @param value text to parse, may be null
     * @return the number, or empty when the text is null or no whole number
     */
    public static Optional<Integer> parseInt(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
