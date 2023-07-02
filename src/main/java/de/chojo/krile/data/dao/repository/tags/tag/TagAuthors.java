package de.chojo.krile.data.dao.repository.tags.tag;

import de.chojo.krile.data.access.Authors;
import de.chojo.krile.data.dao.Author;
import de.chojo.krile.tagimport.tag.RawTag;
import de.chojo.krile.tagimport.tag.entities.RawAuthor;
import org.intellij.lang.annotations.Language;

import java.util.Optional;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class TagAuthors {
    private final TagMeta meta;
    private final Authors authors;

    public TagAuthors(TagMeta meta, Authors authors) {
        this.meta = meta;
        this.authors = authors;
    }

    public void update(RawTag tag) {
        clear();

        for (RawAuthor raw : tag.fileMeta().authors()) {
            Optional<Author> author = authors.getOrCreate(raw);
            if (author.isEmpty()) continue;
            assign(author.get());
        }
    }

    public void assign(Author author) {
        @Language("postgresql")
        var insert = """
                INSERT INTO tag_author(tag_id, author_id) VALUES(?,?)""";
        builder().query(insert)
                .parameter(stmt -> stmt.setInt(meta.tag().id()).setInt(author.id()))
                .insert()
                .sendSync();
    }

    public void clear() {
        builder().query("DELETE FROM tag_author where tag_id = ?")
                .parameter(stmt -> stmt.setInt(meta.tag().id()))
                .delete()
                .sendSync();
    }
}
