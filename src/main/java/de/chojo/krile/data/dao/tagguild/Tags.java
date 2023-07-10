/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao.tagguild;

import de.chojo.jdautil.parsing.ValueParser;
import de.chojo.krile.data.access.AuthorData;
import de.chojo.krile.data.access.CategoryData;
import de.chojo.krile.data.dao.TagGuild;
import de.chojo.krile.data.dao.repository.tags.Tag;
import de.chojo.sadu.wrapper.util.Row;
import org.intellij.lang.annotations.Language;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class Tags {
    private final TagGuild guild;
    private final Repositories repositories;
    private final CategoryData categories;
    private final AuthorData authors;

    public Tags(TagGuild guild, Repositories repositories, CategoryData categories, AuthorData authors) {
        this.guild = guild;
        this.repositories = repositories;
        this.categories = categories;
        this.authors = authors;
    }

    public List<CompletedTag> complete(String value) {
        @Language("postgresql")
        var select = """
                WITH ranked_tags AS
                         (SELECT row_number() OVER (PARTITION BY tag ORDER BY gr.priority * rt.global_prio DESC) AS rank,
                                 gr.repository_id,
                                 gr.priority                                                                     AS repo_prio,
                                 global_prio,
                                 rt.id,
                                 tag,
                                 rt.prio                                                                     AS tag_prio,
                                 r.identifier
                          FROM guild_repository gr
                                   LEFT JOIN repo_tags rt ON gr.repository_id = rt.repository_id
                                   LEFT JOIN repository r ON gr.repository_id = r.id
                          WHERE global_prio = 1
                            AND tag ILIKE ('%' || ? || '%')
                            AND gr.guild_id = ?)
                SELECT id, CASE WHEN rank = 1 THEN tag ELSE tag || ' (' || identifier || ')' END AS name
                FROM ranked_tags
                LIMIT 25;
                """;

        return builder(CompletedTag.class)
                .query(select)
                .parameter(stmt -> stmt.setString(value).setLong(guild.id()))
                .readRow(CompletedTag::build)
                .allSync();
    }

    public Optional<Tag> resolveTag(String tagNameOrId) {
        Optional<Integer> id = ValueParser.parseInt(tagNameOrId);
        if (id.isPresent()) {
            return getById(id.get());
        }
        return getByName(tagNameOrId);
    }

    public Optional<Tag> getByName(String name) {
        @Language("postgresql")
        var select = """
                WITH ranked_tags AS
                         (SELECT row_number() OVER (PARTITION BY tag ORDER BY gr.priority * rt.global_prio DESC) AS rank,
                                 gr.repository_id,
                                 gr.priority                                                                     AS repo_prio,
                                 global_prio,
                                 rt.id,
                                 tag,
                                 rt.prio                                                                         AS tag_prio,
                                 r.identifier
                          FROM guild_repository gr
                                   LEFT JOIN repo_tags rt ON gr.repository_id = rt.repository_id
                                   LEFT JOIN repository r ON gr.repository_id = r.id
                          WHERE global_prio = 1
                            AND tag = ?
                            AND gr.guild_id = ?)
                SELECT t.repository_id, t.id, tag_id, t.tag, content
                FROM ranked_tags rt
                LEFT JOIN tag t ON rt.id = t.id
                LIMIT 1;
                """;
        return builder(Tag.class)
                .query(select)
                .parameter(stmt -> stmt.setString(name).setLong(guild.id()))
                .readRow(row -> Tag.build(row, repositories.byId(row.getInt("repository_id")).get(), categories, authors))
                .firstSync();
    }

    public Optional<Tag> getById(int tag) {
        @Language("postgresql")
        var select = """
                SELECT t.repository_id, id, tag_id, tag, content
                FROM tag t
                         LEFT JOIN guild_repository gr ON t.repository_id = gr.repository_id
                WHERE id = ?
                  AND guild_id = ?""";
        return builder(Tag.class)
                .query(select)
                .parameter(stmt -> stmt.setInt(tag).setLong(guild.id()))
                .readRow(row -> Tag.build(row, repositories.byId(row.getInt("repository_id")).get(), categories, authors))
                .firstSync();
    }

    public void used(Tag tag) {
        @Language("postgresql")
        var insert = """
                INSERT INTO tag_stat AS s (guild_id, tag_id)
                VALUES (?, ?)
                ON CONFLICT (guild_id, tag_id)
                DO UPDATE SET views = s.views + 1""";
        builder()
                .query(insert)
                .parameter(stmt -> stmt.setLong(guild.id()).setInt(tag.id()))
                .insert()
                .send();
    }

    public int count() {
        @Language("postgresql")
        var select = """
                SELECT count(1)
                FROM guild_repository gr
                         LEFT JOIN tag t ON gr.repository_id = t.repository_id
                WHERE guild_id = ?""";
        return builder(Integer.class)
                .query(select)
                .parameter(stmt -> stmt.setLong(guild.id()))
                .map()
                .firstSync()
                .orElse(0);
    }

    public List<RankedTag> rankingPage(int page, int size) {
        @Language("postgresql")
        var select = """
                WITH ranked_tags
                         AS (SELECT row_number() OVER (PARTITION BY tag ORDER BY gr.priority DESC) AS duplicate,
                                    dense_rank() OVER (ORDER BY views DESC NULLS LAST) AS rank,
                                    gr.priority,
                                    t.id,
                                    tag,
                                    identifier,
                                    coalesce(views, 0)                                             AS views
                             FROM guild_repository gr
                                      LEFT JOIN tag t ON gr.repository_id = t.repository_id
                                      LEFT JOIN tag_stat s ON t.id = s.tag_id
                                      LEFT JOIN repository r ON gr.repository_id = r.id
                             WHERE gr.guild_id = ?
                             ORDER BY views DESC, priority DESC)
                SELECT id, rank, CASE WHEN duplicate = 1 THEN tag ELSE tag || ' (' || identifier || ')' END AS name, views
                FROM ranked_tags
                LIMIT ? OFFSET ?""";
        return builder(RankedTag.class)
                .query(select)
                .parameter(stmt -> stmt.setLong(guild.id()).setInt(size).setInt(size * page))
                .readRow(row -> new RankedTag(row.getInt("rank"), row.getString("name"), row.getInt("views")))
                .allSync();
    }

    public Optional<Tag> random() {
        @Language("postgresql")
        var select = """
                SELECT gr.repository_id, t.id,  tag_id, tag, content
                FROM guild_repository gr
                         LEFT JOIN tag t ON gr.repository_id = t.repository_id
                WHERE guild_id = ?
                ORDER BY random()
                LIMIT 1""";
        return builder(Tag.class)
                .query(select)
                .parameter(stmt -> stmt.setLong(guild.id()))
                .readRow(row -> Tag.build(row, repositories.byId(row.getInt("repository_id")).get(), categories, authors))
                .firstSync();
    }

    public record RankedTag(int rank, String tag, int views) {
        @Override
        public String toString() {
            return "`%d`. %s - %d".formatted(rank, tag, views);
        }
    }

    public record CompletedTag(int id, String name) {
        public static CompletedTag build(Row row) throws SQLException {
            return new CompletedTag(row.getInt("id"), row.getString("name"));
        }
    }
}
