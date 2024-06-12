/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.access;

import de.chojo.krile.data.dao.repository.tags.Tag;
import de.chojo.krile.data.util.CompletedCategory;
import de.chojo.krile.data.util.TagFilter;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class TagData {
    private final RepositoryData repositoryData;
    private final CategoryData categoryData;
    private final AuthorData authorData;

    public TagData(RepositoryData repositoryData, CategoryData categoryData, AuthorData authorData) {
        this.repositoryData = repositoryData;
        this.categoryData = categoryData;
        this.authorData = authorData;
    }

    /**
     * Returns a random Tag from the database.
     *
     * @return An Optional object representing a Tag. The optional value may be empty if no Tag is found.
     */
    public Optional<Tag> random() {
        @Language("postgresql")
        var select = """
                SELECT
                    gr.repository_id,
                    t.id,
                    tag_id,
                    tag,
                    content
                FROM
                    guild_repository gr
                        LEFT JOIN tag t
                        ON gr.repository_id = t.repository_id
                ORDER BY random()
                LIMIT 1""";
        return query(select)
                .single()
                .map(row -> Tag.build(row, repositoryData.byId(row.getInt("repository_id")).get(), categoryData, authorData))
                .first();
    }

    /**
     * Searches for tags based on the given filter criteria.
     *
     * @param filter The filter criteria used to search for tags.
     *               The filter criteria include:
     *               - name: The name of the tag to search for (optional).
     *               - language: The language of the tag to search for (optional).
     *               - category: The category ID of the tag to search for (optional).
     * @return A list of Tag objects matching the given filter criteria.
     */
    public List<Tag> search(TagFilter filter) {
        @Language("postgresql")
        var select = """
                WITH
                    public_tags
                        AS (
                        SELECT
                            t.repository_id,
                            id,
                            tag
                        FROM
                            repo_tags t
                                LEFT JOIN repository_meta m
                                ON t.repository_id = m.repository_id
                        WHERE public
                          AND ( tag ILIKE ? OR ? IS NULL )
                          AND ( language ILIKE ? OR ? IS NULL )
                    ),
                    categories
                        AS (
                        SELECT DISTINCT
                            id,
                            tag,
                            repository_id
                        FROM
                            public_tags rt
                                LEFT JOIN tag_category tc
                                ON rt.id = tc.category_id
                        WHERE ( category_id = ? OR ? IS NULL )
                    )
                SELECT DISTINCT
                    c.repository_id,
                    c.id,
                    tag_id,
                    r.tag,
                    content
                FROM
                    categories c
                        LEFT JOIN repository_meta rm
                        ON c.repository_id = rm.repository_id
                        LEFT JOIN tag r
                        ON c.id = r.id
                LIMIT 50;
                                """;
        return query(select)
                .single(call()
                        .bind(filter.name()).bind(filter.name())
                        .bind(filter.language()).bind(filter.language())
                        .bind(filter.category()).bind(filter.category())
                ).map(row -> Tag.build(row, repositoryData.byId(row.getInt("repository_id")).get(), categoryData, authorData))
                .all();

    }

    /**
     * Returns a list of completed categories based on the provided value.
     *
     * @param value the value to search for categories (case-insensitive)
     * @return a list of CompletedCategory objects representing the completed categories
     */
    public List<CompletedCategory> completeCategories(String value) {
        @Language("postgresql")
        var select = """
                SELECT
                    DISTINCT category,
                    c.id
                FROM
                    repository_meta m
                        LEFT JOIN tag t
                        ON m.repository_id = t.repository_id
                        LEFT JOIN tag_category tc
                        ON t.id = tc.tag_id
                        LEFT JOIN category c
                        ON tc.category_id = c.id
                WHERE public
                  AND category ILIKE '%%' || ? || '%%'
                LIMIT 25""";
        return query(select)
                .single(call().bind(value))
                .map(row -> new CompletedCategory(row.getInt("id"), row.getString("category")))
                .all();
    }

    /**
     * Retrieves a {@link Tag} based on its ID.
     *
     * @param id The ID of the tag to retrieve.
     * @return An {@link Optional} containing the {@link Tag} if found, or an empty {@link Optional} if not found.
     */
    public Optional<Tag> byId(int id) {
        @Language("postgresql")
        var select = """
                SELECT t.repository_id, id, tag_id, tag, content
                FROM tag t
                         LEFT JOIN guild_repository gr ON t.repository_id = gr.repository_id
                WHERE id = ?""";
        return query(select)
                .single(call().bind(id))
                .map(row -> Tag.build(row, repositoryData.byId(row.getInt("repository_id")).get(), categoryData, authorData))
                .first();
    }
}
