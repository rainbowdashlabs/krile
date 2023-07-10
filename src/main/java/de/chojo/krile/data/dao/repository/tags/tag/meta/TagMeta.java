/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao.repository.tags.tag.meta;

import org.jetbrains.annotations.Nullable;

public record TagMeta(@Nullable String image, TagType type) {
}
