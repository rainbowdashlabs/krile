package de.chojo.krile.tag.entities;

import java.util.Optional;

public record FileMeta(String fileName, Optional<FileEvent> created, Optional<FileEvent> modified) {
}
