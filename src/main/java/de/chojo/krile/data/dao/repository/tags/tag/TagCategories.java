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

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

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
        query(insert)
                .single(call().bind(meta.tag().id()).bind(category.id()))
                .insert();
    }

    /**
     * Clears the tag category.
     * Removes all records from the "tag_category" table where the tag id matches the given tag id.
     */
    public void clear() {
        query("DELETE FROM tag_category WHERE tag_id = ?")
                .single(call().bind(meta.tag().id()))
                .delete();
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
            categories = query(select)
                    .single(call().bind(meta.tag().id()))
                    .map(row -> categoryData.get(row.getInt("id")).get())
                    .all();
        }
        return categories;
    }
}
