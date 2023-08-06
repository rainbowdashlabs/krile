/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

public class Text {
    private static final Pattern NEW_LINE = Pattern.compile("(?:(?<! {2})(?<!\\\\))(?<!\\R)\\R(?!^\\R)", Pattern.MULTILINE + Pattern.UNICODE_CASE);
    private static final Pattern TRAILING_LINE_END = Pattern.compile("(\\\\|\\s\\s)$", Pattern.MULTILINE + Pattern.UNICODE_CASE);
    private static final Pattern FUSE_CODE_BLOCKS = Pattern.compile("```\\R```", Pattern.MULTILINE + Pattern.UNICODE_CASE);
    private static final Pattern PULL_TEXT_TO_CODE = Pattern.compile("```E\\R", Pattern.MULTILINE + Pattern.UNICODE_CASE);

    /**
     * Splits the given text into chunks of maximum length specified by maxLength parameter.
     *
     * @param text      - The text to be split.
     * @param maxLength - The maximum length of each chunk.
     * @return A list of strings, each representing a chunk of the original text.
     */
    public static List<String> splitByLength(String text, int maxLength) {
        List<String> lines = text.lines().toList();
        return compressChunks(lines, maxLength, "\n").stream().toList();
    }

    public static List<String> compressChunks(List<String> chunks, int maxLength, String delimiter) {
        List<String> join = new LinkedList<>();
        List<String> split = new ArrayList<>();
        int length = 0;
        for (String line : chunks) {
            if (length + line.length() > maxLength) {
                split.add(String.join(delimiter, join));
                join.clear();
                length = 0;
            }
            length += line.length();
            join.add(line);
        }
        split.add(String.join(delimiter, join));
        return split;
    }

    public static String compressCodeBlocks(String code) {
        String compressed = FUSE_CODE_BLOCKS.matcher(code).replaceAll("``````");
        compressed = PULL_TEXT_TO_CODE.matcher(compressed).replaceAll("```");
        return compressed.replaceAll("```E", "```");
    }

    public static List<String> toDiscordMarkdownAndSplit(String text, int maxLength) {
        List<String> blocks = new LinkedList<>();
        boolean codeBlock = false;
        var collect = new ArrayList<String>();
        // Aggregate lines. Strip line breaks outside block quotes.
        for (String line : text.lines().toList()) {
            if (line.startsWith("```") || line.endsWith("```")) {
                if (codeBlock) {
                    // code block has ended
                    collect.add(line +"E");
                    blocks.addAll(process(collect, codeBlock, maxLength));
                    collect.clear();
                } else {
                    // code block is about to start
                    blocks.addAll(process(collect, codeBlock, maxLength));
                    collect.clear();
                    collect.add(line);
                }
                codeBlock = !codeBlock;
            } else {
                collect.add(line);
            }
        }

        blocks.addAll(process(collect, codeBlock, maxLength));
        blocks = blocks.stream().filter(p -> !p.isBlank()).toList();
        blocks = compressChunks(blocks, maxLength, "\n");
        return blocks.stream().map(Text::compressCodeBlocks).toList();
    }

    private static List<String> process(List<String> text, boolean codeBlock, int maxLength) {
        // preserve codeblocks
        // split and strip text
        return codeBlock ? Collections.singletonList(String.join("\n", text)) : stripTextLineBreaks(String.join("\n",text), maxLength);
    }

    public static List<String> stripTextLineBreaks(String text, int maxLength) {
        /*
        This has to be one line.
        This has to be one line as well\
        This line
        and this line are together.

        This has to be a new section.
         */

        // Line breaks in markdown are different from discord.
        // A line break in markdown has to be two spaces at the end
        // A new section in markdown are two new lines.

        String cleaned = NEW_LINE.matcher(text).replaceAll(" ");
        // Super nice edge case of the regex
        if(cleaned.startsWith(" ")) cleaned = cleaned.replaceAll("^ ", "\n");
        cleaned = TRAILING_LINE_END.matcher(cleaned).replaceAll("");

        return splitByLength(cleaned, maxLength);
    }
}
