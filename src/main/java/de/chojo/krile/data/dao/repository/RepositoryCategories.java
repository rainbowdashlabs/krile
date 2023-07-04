package de.chojo.krile.data.dao.repository;

import de.chojo.krile.data.access.Categories;
import de.chojo.krile.data.dao.Category;
import de.chojo.krile.tagimport.repo.RawRepository;
import org.intellij.lang.annotations.Language;

import java.util.Optional;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class RepositoryCategories {
    private final Meta meta;
    private final Categories categories;

    public RepositoryCategories(Meta meta, Categories categories) {
        this.meta = meta;
        this.categories = categories;
    }

    public void updateCategories(RawRepository repository) {
        // Clear repository category links
        clearCategories();

        for (String name : repository.configuration().category()) {
            Optional<Category> category = categories.getOrCreate(name);
            if (category.isEmpty()) continue;
            assignCategory(category.get());
        }
    }

    public void assignCategory(Category category) {
        @Language("postgresql")
        var insert = """
                INSERT INTO repository_category(repository_id, category_id) VALUES(?,?)""";
        builder().query(insert)
                .parameter(stmt -> stmt.setInt(meta.repository().id()).setInt(category.id()))
                .insert()
                .sendSync();
    }

    public void clearCategories() {
        builder().query("DELETE FROM repository_category where repository_id = ?")
                .parameter(stmt -> stmt.setInt(meta.repository().id()))
                .delete()
                .sendSync();
    }
}
