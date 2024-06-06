/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.access;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.jdautil.parsing.ValueParser;
import de.chojo.krile.data.dao.Category;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class CategoryData {
    private final Cache<Integer, Category> categpryCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();

    public Optional<Category> getOrCreate(String name) {
        return get(name).or(() -> create(name));
    }

    /**
     * Creates a new category with the given name.
     *
     * @param name the name of the category to create
     * @return an Optional object which may contain the created Category, or empty if the creation fails
     */
    public Optional<Category> create(String name) {
        @Language("postgresql")
        var query = """
                INSERT INTO category(category) VALUES(lower(?))
                    ON CONFLICT(lower(category))
                    DO NOTHING
                    RETURNING id, category""";
        return cache(query(query)
                .single(call().bind(harmonize(name)))
                .map(Category::build)
                .first());
    }

    private Category cache(Category category) {
        categpryCache.put(category.id(), category);
        return category;
    }

    private Optional<Category> cache(Optional<Category> category) {
        category.ifPresent(this::cache);
        return category;
    }

    /**
     * Resolves a category based on the given ID or name.
     *
     * @param idOrName the ID or name of the category to resolve
     * @return an Optional object which may contain the resolved Category, or empty if the resolution fails
     */
    public Optional<Category> resolve(String idOrName) {
        Optional<Integer> id = ValueParser.parseInt(idOrName);
        if (id.isPresent()) {
            return get(id.get());
        }
        return get(idOrName);
    }

    /**
     * Retrieves a category with the given name.
     *
     * @param name the name of the category to retrieve
     * @return an Optional object which may contain the retrieved Category, or empty if the category does not exist
     */
    public Optional<Category> get(String name) {
        @Language("postgresql")
        var query = """
                SELECT id, c.category FROM category c WHERE lower(?) = category""";
        return cache(query(query)
                .single(call().bind(harmonize(name)))
                .map(Category::build)
                .first());
    }

    /**
     * Retrieves a category from the database with the specified ID.
     *
     * @param id the ID of the category to retrieve
     * @return an Optional object which may contain the retrieved Category, or empty if no category is found with the specified ID
     */
    public Optional<Category> get(int id) {
        @Language("postgresql")
        var query = """
                SELECT id, c.category FROM category c WHERE id = ?""";
        return cache(query(query)
                .single(call().bind(id))
                .map(Category::build)
                .first());
    }

    private String harmonize(@Nullable String name) {
        if (name == null) return null;
        return name.replace("_", " ").toLowerCase(Locale.ROOT);
    }
}
