package de.chojo.krile.data.dao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class IdentifierTest {

    @Test
    void testToString() {
        Identifier of = Identifier.of("platform", "user", "repo");
        assertEquals("platform:user/repo", of.toString());
        of = Identifier.of("platform", "user", "repo", "/path/to/repo");
        assertEquals("platform:user/repo//path/to/repo", of.toString());
    }

    @Test
    void parseNormal() {
        Optional<Identifier> parse = Identifier.parse("platform:user/repo");
        assertTrue(parse.isPresent());
        assertEquals("platform", parse.get().platform());
        assertEquals("user", parse.get().user());
        assertEquals("repo", parse.get().repo());
        assertNull(parse.get().path());
    }

    @Test
    void parseSub() {
        Optional<Identifier> parse = Identifier.parse("platform:user/repo//path/to/repo");
        assertTrue(parse.isPresent());
        assertEquals("platform", parse.get().platform());
        assertEquals("user", parse.get().user());
        assertEquals("repo", parse.get().repo());
        assertEquals("/path/to/repo", parse.get().path());
    }
}
