package de.chojo.krile.tagimport.tag;

import de.chojo.krile.tagimport.tag.entities.FileMeta;
import de.chojo.krile.tagimport.tag.entities.RawTagMeta;
import de.chojo.krile.util.Text;

import java.util.List;

public record RawTag(RawTagMeta meta, FileMeta fileMeta, String text) {
    public List<String> splitText(){
        return Text.splitByLength(text, 3500);
    }
}
