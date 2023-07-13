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
    private final Meta meta;
    private final AuthorData authorData;
    private List<Author> authors;

    public TagAuthors(Meta meta, AuthorData authorData) {
        this.meta = meta;
        this.authorData = authorData;
    }

    /**
     * Updates the current tag with new data.
     *
     * @param tag the RawTag object containing the new data
     */
    public void update(RawTag tag) {
        clear();

        for (RawAuthor raw : tag.fileMeta().authors()) {
            Optional<Author> author = authorData.getOrCreate(raw);
            if (author.isEmpty()) continue;
            assign(author.get());
        }
    }

    /**
     * Assigns the given Author to a Tag.
     *
     * @param author The Author to assign to the Tag.
     * @see Author
     */
    public void assign(Author author) {
        @Language("postgresql")
        var insert = """
                INSERT INTO tag_author(tag_id, author_id) VALUES(?,?)""";
        builder().query(insert)
                .parameter(stmt -> stmt.setInt(meta.tag().id()).setInt(author.id()))
                .insert()
                .sendSync();
    }

    /**
     * Clears the tag_author table by deleting all records with the given tag ID.
     */
    public void clear() {
        builder().query("DELETE FROM tag_author WHERE tag_id = ?")
                .parameter(stmt -> stmt.setInt(meta.tag().id()))
                .delete()
                .sendSync();
    }

    /**
     * Retrieves a list of all authors associated with a specific tag.
     *
     * @return A list of Author objects representing the authors associated with the tag.
     */
    public List<Author> all() {
        if (authors == null) {
            @Language("postgresql")
            var select = """
                    SELECT id, name, mail
                    FROM tag_author
                             LEFT JOIN author a ON a.id = tag_author.author_id
                    WHERE tag_id = ?""";
            authors = builder(Author.class)
                    .query(select)
                    .parameter(stmt -> stmt.setInt(meta.tag().id()))
                    .readRow(row -> authorData.get(row.getInt("id")).get())
                    .allSync();
        }
        return authors;
    }
}
