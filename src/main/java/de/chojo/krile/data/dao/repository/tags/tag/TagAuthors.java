/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao.repository.tags.tag;

import de.chojo.krile.data.access.AuthorData;
import de.chojo.krile.data.dao.Author;
import de.chojo.krile.tagimport.tag.RawTag;
import de.chojo.krile.tagimport.tag.entities.RawAuthor;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.Optional;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class TagAuthors {
    private final TagMeta meta;
    private final AuthorData authors;

    public TagAuthors(TagMeta meta, AuthorData authors) {
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

    public List<Author> all() {
        @Language("postgresql")
        var select = """
                SELECT id, name, mail
                FROM tag_author
                         LEFT JOIN author a ON a.id = tag_author.author_id
                WHERE tag_id = ?""";
        return builder(Author.class)
                .query(select)
                .parameter(stmt -> stmt.setInt(meta.tag().id()))
                .readRow(Author::build)
                .allSync();
    }
}
