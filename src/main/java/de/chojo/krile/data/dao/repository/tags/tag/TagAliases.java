package de.chojo.krile.data.dao.repository.tags.tag;

import de.chojo.krile.tagimport.tag.RawTag;
import org.intellij.lang.annotations.Language;

import java.util.List;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class TagAliases {
    private final TagMeta meta;

    public TagAliases(TagMeta meta) {
        this.meta = meta;
    }

    public void update(RawTag tag) {
        // Clear repository aliases
        clear();

        for (String alias : tag.meta().alias()) {
            assign(alias);
        }
    }

    public void assign(String alias) {
        @Language("postgresql")
        var insert = """
                INSERT INTO tag_alias(tag_id, alias) VALUES(?,?)""";
        builder().query(insert)
                .parameter(stmt -> stmt.setInt(meta.tag().id()).setString(alias))
                .insert()
                .sendSync();
    }

    public void clear() {
        builder().query("DELETE FROM tag_alias where tag_id = ?")
                .parameter(stmt -> stmt.setInt(meta.tag().id()))
                .delete()
                .sendSync();
    }

    public List<String> all() {
        @Language("postgresql")
        var select = """
                SELECT alias FROM tag_alias WHERE tag_id = ?""";

        return builder(String.class)
                .query(select)
                .parameter(stmt -> stmt.setInt(meta.tag().id()))
                .readRow(row -> row.getString("alias"))
                .allSync();
    }
}
