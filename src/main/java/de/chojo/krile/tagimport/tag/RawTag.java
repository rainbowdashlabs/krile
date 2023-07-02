package de.chojo.krile.tagimport.tag;

import de.chojo.krile.tagimport.tag.entities.FileMeta;
import de.chojo.krile.tagimport.tag.entities.RawTagMeta;

public record RawTag(RawTagMeta meta, FileMeta fileMeta, String text) {
}
