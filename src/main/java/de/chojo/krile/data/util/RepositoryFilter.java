/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.util;

import org.jetbrains.annotations.Nullable;

public record RepositoryFilter(Integer category,
                               String language,
                               String name,
                               String platform,
                               String user,
                               String repo,
                               Integer tags) {
    @Override
    public String language() {
        return search(language);
    }

    @Override
    public String platform() {
        return search(platform);
    }

    @Override
    public String user() {
        return search(user);
    }

    public String name() {
        return search(name);
    }

    @Override
    public String repo() {
        return search(repo);
    }

    private String search(@Nullable String value) {
        return value == null ? null : "%%%s%%".formatted(value);
    }
}
