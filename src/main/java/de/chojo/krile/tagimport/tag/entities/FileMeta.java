package de.chojo.krile.tagimport.tag.entities;

import java.util.Collection;
import java.util.Optional;

public record FileMeta(String fileName, Collection<RawAuthor> authors, FileEvent created,
                       FileEvent modified) {
}
