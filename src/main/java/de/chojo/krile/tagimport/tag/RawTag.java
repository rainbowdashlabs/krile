/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.tagimport.tag;

import de.chojo.krile.tagimport.tag.entities.FileMeta;
import de.chojo.krile.tagimport.tag.entities.RawTagMeta;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static de.chojo.krile.util.Text.splitByLength;
import static de.chojo.krile.util.Text.toDiscordMarkdownAndSplit;

public record RawTag(RawTagMeta meta, FileMeta fileMeta, String text) {
    private static final int MAX_LENGTH = 1850;
    /**
     * Splits the given text by the "<new_page>" delimiter and breaks each substring into smaller chunks,
     * each having a maximum length of 1850 characters.
     *
     * @return A list of strings, where each string represents a chunk of the original text.
     */
    public List<String> splitText() {
        return Arrays.stream(text.split("<new_page>"))
                .map(t -> meta.enhanceMarkdown() ? toDiscordMarkdownAndSplit(t, MAX_LENGTH) : splitByLength(t, MAX_LENGTH))
                .flatMap(Collection::stream)
                .toList();
    }
}
