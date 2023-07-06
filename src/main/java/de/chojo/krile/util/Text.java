/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Text {
    public static List<String> splitByLength(String text, int maxLength) {
        StringJoiner joiner = new StringJoiner("\n");
        List<String> split = new ArrayList<>();
        List<String> lines = text.lines().toList();
        for (String line : lines) {
            if (joiner.length() + line.length() > maxLength) {
                split.add(joiner.toString());
                joiner = new StringJoiner("\n");
            }
            joiner.add(line);
        }
        split.add(joiner.toString());
        return split;
    }
}
