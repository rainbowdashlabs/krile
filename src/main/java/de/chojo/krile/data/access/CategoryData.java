/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.access;

import de.chojo.jdautil.parsing.ValueParser;
import de.chojo.krile.data.dao.Category;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class CategoryData {
    public Optional<Category> getOrCreate(String name) {
        return get(name).or(() -> create(name));
    }

    public Optional<Category> create(String name) {
        @Language("postgresql")
        var query = """
                INSERT INTO category(category) VALUES(lower(?))
                    ON CONFLICT(lower(category))
                    DO NOTHING
                    RETURNING id, category""";
        return builder(Category.class)
                .query(query)
                .parameter(stmt -> stmt.setString(harmonize(name)))
                .readRow(Category::build)
                .firstSync();
    }

    public Optional<Category> resolve(String idOrName) {
        Optional<Integer> id = ValueParser.parseInt(idOrName);
        if (id.isPresent()) {
            return get(id.get());
        }
        return get(idOrName);
    }

    public Optional<Category> get(String name) {
        @Language("postgresql")
        var query = """
                SELECT id, c.category FROM category c WHERE lower(?) = category""";
        return builder(Category.class)
                .query(query)
                .parameter(stmt -> stmt.setString(harmonize(name)))
                .readRow(Category::build)
                .firstSync();
    }

    public Optional<Category> get(int id) {
        @Language("postgresql")
        var query = """
                SELECT id, c.category FROM category c WHERE id = ?""";
        return builder(Category.class)
                .query(query)
                .parameter(stmt -> stmt.setInt(id))
                .readRow(Category::build)
                .firstSync();
    }

    private String harmonize(@Nullable String name) {
        if (name == null) return null;
        return name.replace("_", " ").toLowerCase(Locale.ROOT);
    }
}
