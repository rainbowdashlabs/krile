/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao.repository.tags.tag;

import de.chojo.krile.data.access.AuthorData;
import de.chojo.krile.data.access.CategoryData;
import de.chojo.krile.data.dao.Author;
import de.chojo.krile.data.dao.repository.tags.Tag;
import de.chojo.krile.data.dao.repository.tags.tag.tagmeta.FileMeta;
import de.chojo.krile.tagimport.tag.RawTag;
import org.intellij.lang.annotations.Language;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class TagMeta {
    private final Tag tag;
    private final TagCategories categories;
    private final TagAuthors tagAuthors;
    private final AuthorData authors;
    private final TagAliases aliases;

    public TagMeta(Tag tag, CategoryData categories, AuthorData authors) {
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

    public void update(RawTag raw) {
        categories.update(raw);
        tagAuthors.update(raw);
        aliases.update(raw);
        @Language("postgresql")
        var insert = """
                INSERT INTO tag_meta(tag_id, file_name, image, created, created_by, modified, modified_by)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (tag_id)
                    DO UPDATE
                    SET image       = excluded.image,
                        file_name = excluded.file_name,
                        created     = excluded.created,
                        created_by  = excluded.created_by,
                        modified    = excluded.modified,
                        modified_by = excluded.modified_by
                """;
        var fileMeta = raw.fileMeta();
        OffsetDateTime created = fileMeta.created().when().atOffset(ZoneOffset.UTC);
        OffsetDateTime modified = fileMeta.modified().when().atOffset(ZoneOffset.UTC);
        Author createdBy = authors.get(fileMeta.created().who()).get();
        Author modifiedBy = authors.get(fileMeta.modified().who()).get();

        builder()
                .query(insert)
                .parameter(stmt -> stmt.setInt(tag.id())
                        .setString(fileMeta.fileName())
                        .setString(raw.meta().image())
                        .setOffsetDateTime(created)
                        .setInt(createdBy.id())
                        .setOffsetDateTime(modified)
                        .setInt(modifiedBy.id()))
                .insert()
                .sendSync();
    }

    public FileMeta fileMeta() {
        @Language("postgresql")
        var select = """
                SELECT file_name,
                       created,
                       ca.id as created_id,
                       ca.mail as created_mail,
                       ca.name as created_name,
                       modified,
                       ma.id as modified_id,
                       ma.mail as modified_mail,
                       ca.name as modified_name
                FROM tag_meta
                LEFT JOIN author ma on tag_meta.modified_by = ma.id
                LEFT JOIN author ca on tag_meta.created_by = ca.id
                WHERE tag_id = ?""";
        return builder(FileMeta.class)
                .query(select)
                .parameter(stmt -> stmt.setInt(tag.id()))
                .readRow(row -> new FileMeta(row.getString("file_name"),
                        row.getLocalDateTime("created").toInstant(ZoneOffset.UTC),
                        Author.build(row, "created_id", "created_name", "created_mail"),
                        row.getLocalDateTime("modified").toInstant(ZoneOffset.UTC),
                        Author.build(row, "modified_id", "modified_name", "modified_mail")
                ))
                .firstSync()
                .get();
    }

    public Tag tag() {
        return tag;
    }

    public Optional<String> image() {
        return builder(String.class)
                .query("SELECT image FROM tag_meta WHERE tag_id = ?")
                .parameter(stmt -> stmt.setInt(tag().id()))
                .readRow(row -> row.getString("image"))
                .firstSync();
    }
}
