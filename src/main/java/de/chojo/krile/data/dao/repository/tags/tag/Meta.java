/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao.repository.tags.tag;

import de.chojo.krile.data.access.AuthorData;
import de.chojo.krile.data.access.CategoryData;
import de.chojo.krile.data.base.BaseMeta;
import de.chojo.krile.data.dao.Author;
import de.chojo.krile.data.dao.repository.tags.Tag;
import de.chojo.krile.data.dao.repository.tags.tag.meta.FileMeta;
import de.chojo.krile.data.dao.repository.tags.tag.meta.TagMeta;
import de.chojo.krile.data.dao.repository.tags.tag.meta.TagType;
import de.chojo.krile.tagimport.tag.RawTag;
import org.intellij.lang.annotations.Language;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;
import static de.chojo.sadu.queries.converter.StandardValueConverter.OFFSET_DATE_TIME;

public class Meta extends BaseMeta {
    private final Tag tag;
    private final TagCategories categories;
    private final TagAuthors tagAuthors;
    private final AuthorData authors;
    private final TagAliases aliases;
    private FileMeta fileMeta;

    public Meta(Tag tag, CategoryData categories, AuthorData authors) {
        this.tag = tag;
        this.categories = new TagCategories(this, categories);
        this.tagAuthors = new TagAuthors(this, authors);
        this.authors = authors;
        this.aliases = new TagAliases(this);
    }

    public TagCategories categories() {
        return categories;
    }

    public TagAuthors tagAuthors() {
        return tagAuthors;
    }

    public TagAliases aliases() {
        return aliases;
    }

    /**
     * Updates the tag with the specified information.
     *
     * @param raw the raw tag object containing the updated information
     */
    public void update(RawTag raw) {
        categories.update(raw);
        tagAuthors.update(raw);
        aliases.update(raw);
        @Language("postgresql")
        var insert = """
                INSERT INTO tag_meta(tag_id, file_name, image, created, created_by, modified, modified_by, type)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (tag_id)
                    DO UPDATE
                    SET image       = excluded.image,
                        file_name   = excluded.file_name,
                        created     = excluded.created,
                        created_by  = excluded.created_by,
                        modified    = excluded.modified,
                        modified_by = excluded.modified_by,
                        type        = excluded.type
                """;
        var fileMeta = raw.fileMeta();
        OffsetDateTime created = fileMeta.created().when().atOffset(ZoneOffset.UTC);
        OffsetDateTime modified = fileMeta.modified().when().atOffset(ZoneOffset.UTC);
        Author createdBy = authors.getOrCreate(fileMeta.created().who()).get();
        Author modifiedBy = authors.getOrCreate(fileMeta.modified().who()).get();

        query(insert)
                .single(call().bind(tag.id())
                        .bind(fileMeta.fileName())
                        .bind(raw.meta().image())
                        .bind(created.toInstant(), INSTANT_TIMESTAMP)
                        .bind(createdBy.id())
                        .bind(modified.toInstant(), INSTANT_TIMESTAMP)
                        .bind(modifiedBy.id())
                        .bind(raw.meta().type()))
                .insert();
        this.fileMeta = null;
        tagMeta = null;
    }

    /**
     * Retrieves the file metadata for the tag.
     *
     * @return the file metadata for the tag, or default values if not found
     */
    public FileMeta fileMeta() {
        if (fileMeta == null) {
            @Language("postgresql")
            var select = """
                    SELECT file_name,
                           created,
                           ca.id AS created_id,
                           ca.mail AS created_mail,
                           ca.name AS created_name,
                           modified,
                           ma.id AS modified_id,
                           ma.mail AS modified_mail,
                           ca.name AS modified_name
                    FROM tag_meta
                    LEFT JOIN author ma ON tag_meta.modified_by = ma.id
                    LEFT JOIN author ca ON tag_meta.created_by = ca.id
                    WHERE tag_id = ?""";
            fileMeta = query(select)
                    .single(call().bind(tag.id()))
                    .map(row -> new FileMeta(row.getString("file_name"),
                            row.get("created", INSTANT_TIMESTAMP),
                            Author.build(row, "created_id", "created_name", "created_mail"),
                            row.get("modified", INSTANT_TIMESTAMP),
                            Author.build(row, "modified_id", "modified_name", "modified_mail")
                    ))
                    .first()
                    .orElseGet(() -> new FileMeta("none", Instant.EPOCH, Author.NONE, Instant.EPOCH, Author.NONE));
        }
        return fileMeta;
    }

    public Tag tag() {
        return tag;
    }

    /**
     * Retrieves the tag metadata for the tag.
     *
     * @return the tag metadata for the tag, or default values if not found
     */
    @Override
    public TagMeta tagMeta() {
        if (tagMeta == null) {
            @Language("postgresql")
            var select = """
                    SELECT
                        image,
                        type
                    FROM
                        tag_meta
                    WHERE tag_id = ?""";
            tagMeta = query(select)
                    .single(call().bind(tag().id()))
                    .map(row -> new TagMeta(row.getString("image"), row.getEnum("type", TagType.class)))
                    .first()
                    .orElseGet(() -> new TagMeta(null, TagType.TEXT));
        }
        return tagMeta;
    }
}
