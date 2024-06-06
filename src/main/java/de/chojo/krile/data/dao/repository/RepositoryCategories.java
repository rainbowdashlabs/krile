/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao.repository;

import de.chojo.krile.data.access.CategoryData;
import de.chojo.krile.data.dao.Category;
import de.chojo.krile.tagimport.exception.ParsingException;
import de.chojo.krile.tagimport.repo.RawRepository;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class RepositoryCategories {
    private final Meta meta;
    private final CategoryData categories;

    public RepositoryCategories(Meta meta, CategoryData categories) {
        this.meta = meta;
        this.categories = categories;
    }

    /**
     * Updates the categories in the given repository based on the category names specified in the repository's configuration.
     *
     * @param repository The repository to update the categories in.
     * @throws ParsingException if there is an error parsing the repository's configuration.
     */
    public void updateCategories(RawRepository repository) throws ParsingException {
        // Clear repository category links
        clearCategories();

        for (String name : repository.configuration().category()) {
            Optional<Category> category = categories.getOrCreate(name);
            if (category.isEmpty()) continue;
            assignCategory(category.get());
        }
    }

    /**
     * Assigns a category to a repository.
     *
     * @param category The category to be assigned.
     */
    public void assignCategory(Category category) {
        @Language("postgresql")
        var insert = """
                INSERT INTO repository_category(repository_id, category_id) VALUES(?,?)""";
        query(insert)
                .single(call().bind(meta.repository().id()).bind(category.id()))
                .insert();
    }

    /**
     * Clears the categories associated with the repository.
     * <p>
     * The method clears the categories associated with the repository by deleting the corresponding records
     * from the 'repository_category' table in the database.
     */
    public void clearCategories() {
        query("DELETE FROM repository_category WHERE repository_id = ?")
                .single(call().bind(meta.repository().id()))
                .delete();
    }

    /**
     * Retrieves all categories for the specified repository.
     *
     * @return A list of Category objects representing the categories.
     */
    public List<Category> all() {
        @Language("postgresql")
        var select = """
                SELECT id, category
                FROM repository_category rc
                         LEFT JOIN category c ON c.id = rc.category_id
                WHERE repository_id = ?""";
        return query(select)
                .single(call().bind(meta.repository().id()))
                .map(Category::build)
                .all();
    }
}
