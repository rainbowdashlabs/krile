/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.tagimport.tag.entities;

import java.util.Collection;
import java.util.Optional;

public record FileMeta(String fileName, Collection<RawAuthor> authors, FileEvent created,
                       FileEvent modified) {
}
