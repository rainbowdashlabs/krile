/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.tagimport.tag.parsing;

import java.util.Optional;

public record TagFile(Optional<String> meta, String content) {
}
