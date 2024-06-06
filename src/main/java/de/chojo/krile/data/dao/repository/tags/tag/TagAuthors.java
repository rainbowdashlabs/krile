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

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

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
        query(insert)
                .single(call().bind(meta.tag().id()).bind(author.id()))
                .insert();
    }

    /**
     * Clears the tag_author table by deleting all records with the given tag ID.
     */
    public void clear() {
        query("DELETE FROM tag_author WHERE tag_id = ?")
                .single(call().bind(meta.tag().id()))
                .delete();
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
            authors = query(select)
                    .single(call().bind(meta.tag().id()))
                    .map(row -> authorData.get(row.getInt("id")).get())
                    .all();
        }
        return authors;
    }
}
