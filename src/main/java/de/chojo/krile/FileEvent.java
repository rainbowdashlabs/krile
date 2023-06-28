package de.chojo.krile;

import java.time.Instant;

public record  FileEvent(Instant time, Author who) {
}
