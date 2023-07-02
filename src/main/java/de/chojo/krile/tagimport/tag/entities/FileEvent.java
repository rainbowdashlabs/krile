package de.chojo.krile.tagimport.tag.entities;

import java.time.Instant;

public record  FileEvent(Instant when, RawAuthor who) {
}
