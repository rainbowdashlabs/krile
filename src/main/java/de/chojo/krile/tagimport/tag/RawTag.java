/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.tagimport.tag;

import de.chojo.krile.tagimport.tag.entities.FileMeta;
import de.chojo.krile.tagimport.tag.entities.RawTagMeta;
import de.chojo.krile.util.Text;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public record RawTag(RawTagMeta meta, FileMeta fileMeta, String text) {
    /**
     * Splits the given text by the "<new_page>" delimiter and breaks each substring into smaller chunks,
     * each having a maximum length of 1850 characters.
     *
     * @return A list of strings, where each string represents a chunk of the original text.
     */
    public List<String> splitText() {
        return Arrays.stream(text.split("<new_page>")).map(t -> Text.toDiscordMarkdownAndSplit(t, 1850)).flatMap(Collection::stream).toList();
    }
}
