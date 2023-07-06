/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.util;

import org.jetbrains.annotations.Nullable;

public record TagFilter(Integer category, String language, String name) {
    @Override
    public String language() {
        return search(language);
    }

    @Override
    public String name() {
        return search(name);
    }

    private String search(@Nullable String value) {
        return value == null ? null : "%%%s%%".formatted(value);
    }

}
