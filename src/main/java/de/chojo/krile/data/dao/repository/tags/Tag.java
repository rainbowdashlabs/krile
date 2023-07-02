package de.chojo.krile.data.dao.repository.tags;

import de.chojo.krile.data.access.Authors;
import de.chojo.krile.data.access.Categories;
import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.data.dao.repository.tags.tag.TagMeta;
import de.chojo.krile.tagimport.tag.RawTag;
import de.chojo.krile.tagimport.tag.entities.FileMeta;
import de.chojo.sadu.wrapper.util.Row;
import org.intellij.lang.annotations.Language;

import java.sql.SQLException;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public final class Tag {
    private final int id;
    private final String tagId;
    private final String tag;
    private final String text;
    private final Repository repository;
    private final TagMeta meta;

    public Tag(int id, String tagId, String tag, String text, Repository repository, Categories categories, Authors authors) {
        this.id = id;
        this.tagId = tagId;
        this.tag = tag;
        this.text = text;
        this.repository = repository;
        this.meta = new TagMeta(this, categories, authors);
    }

    public static Tag build(Row row, Repository repository, Categories categories, Authors authors) throws SQLException {
        return new Tag(row.getInt("id"), row.getString("tag_id"), row.getString("tag"), row.getString("text"), repository, categories, authors);
    }

    public boolean delete() {
        return builder()
                .query("DELETE FROM tag WHERE id = ?")
                .parameter(stmt -> stmt.setInt(id()))
                .delete()
                .sendSync()
                .changed();
    }

    public int id() {
        return id;
    }

    public String tagId() {
        return tagId;
    }

    public String tag() {
        return tag;
    }

    public void update(RawTag raw) {
        @Language("postgresql")
        var insert = """
                UPDATE tag SET content = ? WHERE id = ?""";

        builder()
                .query(insert)
                .parameter(stmt -> stmt.setString(raw.text()).setInt(id()))
                .update()
                .sendSync();
        meta.update(raw);
        FileMeta fileMeta = raw.fileMeta();
        raw.meta();
        raw.text();
    }
}
