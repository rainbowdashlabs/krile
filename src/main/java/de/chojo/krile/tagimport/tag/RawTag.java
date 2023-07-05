package de.chojo.krile.tagimport.tag;

import de.chojo.krile.tagimport.tag.entities.FileMeta;
import de.chojo.krile.tagimport.tag.entities.RawTagMeta;
import de.chojo.krile.util.Text;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public record RawTag(RawTagMeta meta, FileMeta fileMeta, String text) {
    public List<String> splitText() {
        return Arrays.stream(text.split("<new_page>")).map(t -> Text.splitByLength(t, 1850)).flatMap(Collection::stream).toList();
    }
}
