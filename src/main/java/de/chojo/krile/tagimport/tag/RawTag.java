package de.chojo.krile.tagimport.tag;

import de.chojo.krile.tagimport.tag.entities.FileMeta;
import de.chojo.krile.tagimport.tag.entities.TagMeta;

public record RawTag(TagMeta meta, FileMeta fileMeta, String text) {
}
