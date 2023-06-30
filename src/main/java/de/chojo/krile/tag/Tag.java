package de.chojo.krile.tag;

import de.chojo.krile.tag.entities.FileMeta;
import de.chojo.krile.tag.entities.TagMeta;

public record Tag(TagMeta meta, FileMeta fileMeta, String text) {
}
