package de.chojo.krile.tagimport.tag.entities;

import java.util.Collection;
import java.util.Optional;

public record FileMeta(String fileName, Collection<Author> authors, Optional<FileEvent> created,
                       Optional<FileEvent> modified) {
}
