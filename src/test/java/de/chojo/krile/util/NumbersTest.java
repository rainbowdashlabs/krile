/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.util;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumbersTest {
    @Test
    void parsesWholeNumbers() {
        assertEquals(Optional.of(5), Numbers.parseInt("5"));
        assertEquals(Optional.of(-12), Numbers.parseInt("-12"));
    }

    @Test
    void rejectsEverythingElse() {
        assertEquals(Optional.empty(), Numbers.parseInt(null));
        assertEquals(Optional.empty(), Numbers.parseInt("abc"));
        assertEquals(Optional.empty(), Numbers.parseInt(" 5"));
        assertEquals(Optional.empty(), Numbers.parseInt("3000000000"));
    }
}
