/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao.repository;

import de.chojo.krile.data.access.AuthorData;
import de.chojo.krile.data.access.CategoryData;
import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.data.dao.repository.tags.Tag;
import de.chojo.krile.tagimport.exception.ImportException;
import de.chojo.krile.tagimport.exception.ParsingException;
import de.chojo.krile.tagimport.repo.RawRepository;
import de.chojo.krile.tagimport.tag.RawTag;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.postgresql.types.PostgreSqlTypes;
import org.intellij.lang.annotations.Language;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class Tags {
    private final Repository repository;
    private final CategoryData categories;
    private final AuthorData authors;

    public Tags(Repository repository, CategoryData categories, AuthorData authors) {
        this.repository = repository;
        this.categories = categories;
        this.authors = authors;
    }

    /**
     * Retrieves all tags from the repository.
     *
     * @return A list of {@link Tag} objects representing the tags retrieved.
     */
    public List<Tag> all() {
        @Language("postgresql")
        var select = """
                SELECT repository_id, id, tag_id, tag, content FROM tag WHERE repository_id = ?""";
        return query(select)
                .single(call().bind(repository.id()))
                .map(this::buildTag)
                .all();
    }

    /**
     * Updates the tags in the given repository.
     *
     * @param repository the repository containing the tags to be updated
     * @throws ImportException  if there is an error importing the tags
     * @throws ParsingException if there is an error parsing the tags
     */
    public void update(RawRepository repository) throws ImportException, ParsingException {
        List<RawTag> tags;
        tags = repository.tags();

        List<Tag> all = all();

        for (RawTag raw : tags) {
            Optional<Tag> optTag = byRawTag(raw).or(() -> create(raw));
            if (optTag.isEmpty()) continue;
            Tag tag = optTag.get();
            tag.update(raw);
            all.removeIf(t -> t.id() == tag.id());
        }

        // Delete all not updated tags.
        all.forEach(Tag::delete);
    }

    /**
     * Creates a new tag in the repository.
     *
     * @param tag the raw tag information
     * @return an Optional containing the created Tag entity, or empty if the creation was unsuccessful
     */
    public Optional<Tag> create(RawTag tag) {
        @Language("postgresql")
        var insert = """
                INSERT
                INTO
                    tag(repository_id, tag_id, tag, content)
                VALUES
                    (?, ?, ?, ?)
                ON CONFLICT(repository_id, tag_id)
                    DO UPDATE
                    SET
                        tag     = excluded.tag,
                        content = excluded.content
                RETURNING id,
                    tag_id,
                    tag,
                    content""";
        return query(insert)
                .single(call().bind(repository.id())
                        .bind(tag.meta().id())
                        .bind(tag.meta().tag())
                        .bind(tag.splitText(), PostgreSqlTypes.TEXT))
                .map(this::buildTag)
                .first();
    }

    /**
     * Retrieves a Tag object based on the given RawTag.
     *
     * @param tag The RawTag to search for.
     * @return An Optional containing the Tag object if found, otherwise an empty Optional.
     */
    public Optional<Tag> byRawTag(RawTag tag) {
        return byTagId(tag.meta().id());
    }

    /**
     * Retrieves a tag with the given tag id.
     *
     * @param tagId The tag id to search for.
     * @return Optional containing the tag with the given tag id if found, otherwise empty.
     */
    public Optional<Tag> byTagId(String tagId) {
        @Language("postgresql")
        var select = """
                SELECT repository_id, id, tag_id, tag, content
                FROM tag
                WHERE tag_id = ?
                  AND repository_id = ?""";
        return query(select)
                .single(call().bind(tagId).bind(repository.id()))
                .map(this::buildTag)
                .first();
    }

    /**
     * Counts the number of records in the 'tag' table with a specific repository ID.
     *
     * @return The count of records in the table, or 0 if no records are found.
     */
    public int count() {
        return query("SELECT count(1) FROM tag WHERE repository_id = ?")
                .single(call().bind(repository.id()))
                .map(row -> row.getInt("count"))
                .first()
                .orElse(0);
    }

    private Tag buildTag(Row row) throws SQLException {
        return Tag.build(row, repository, categories, authors);
    }
}
