/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao.repository.tags.tag;

import de.chojo.krile.data.access.CategoryData;
import de.chojo.krile.data.dao.Category;
import de.chojo.krile.tagimport.tag.RawTag;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.Optional;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class TagCategories {
    private final Meta meta;
    private final CategoryData categoryData;
    private List<Category> categories;

    public TagCategories(Meta meta, CategoryData categoryData) {
        this.meta = meta;
        this.categoryData = categoryData;
    }

    /**
     * Updates the given RawTag by clearing the repository category links and assigning it to the respective categories.
     *
     * @param tag the RawTag to update
     */
    public void update(RawTag tag) {
        // Clear repository category links
        clear();

        for (String name : tag.meta().category()) {
            Optional<Category> category = categoryData.getOrCreate(name);
            if (category.isEmpty()) continue;
            assign(category.get());
        }
    }

    /**
     * Assigns the given category to the tag.
     *
     * @param category the category to be assigned
     */
    public void assign(Category category) {
        @Language("postgresql")
        var insert = """
                INSERT INTO tag_category(tag_id, category_id) VALUES(?,?)""";
        builder().query(insert)
                .parameter(stmt -> stmt.setInt(meta.tag().id()).setInt(category.id()))
                .insert()
                .sendSync();
    }

    /**
     * Clears the tag category.
     * Removes all records from the "tag_category" table where the tag id matches the given tag id.
     */
    public void clear() {
        builder().query("DELETE FROM tag_category WHERE tag_id = ?")
                .parameter(stmt -> stmt.setInt(meta.tag().id()))
                .delete()
                .sendSync();
    }

    /**
     * Retrieves all categories.
     *
     * @return A list of categories.
     */
    public List<Category> all() {
        if (categories == null) {
            @Language("postgresql")
            var select = """
                    SELECT id, category
                    FROM tag_category
                             LEFT JOIN category c ON c.id = tag_category.category_id
                    WHERE tag_id = ?""";
            categories = builder(Category.class)
                    .query(select)
                    .parameter(stmt -> stmt.setInt(meta.tag().id()))
                    .readRow(row -> categoryData.get(row.getInt("id")).get())
                    .allSync();
        }
        return categories;
    }
}
