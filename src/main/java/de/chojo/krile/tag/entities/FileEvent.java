package de.chojo.krile.tag.entities;

import java.time.Instant;

public record  FileEvent(Instant when, Author who) {
}
