package de.chojo.krile.data.dao.repository.tags.tag;

import de.chojo.krile.data.access.CategoryData;
import de.chojo.krile.data.dao.Category;
import de.chojo.krile.tagimport.tag.RawTag;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.Optional;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class TagCategories {
    private final TagMeta meta;
    private final CategoryData categories;

    public TagCategories(TagMeta meta, CategoryData categories) {
        this.meta = meta;
        this.categories = categories;
    }

    public void update(RawTag tag) {
        // Clear repository category links
        clear();

        for (String name : tag.meta().category()) {
            Optional<Category> category = categories.getOrCreate(name);
            if (category.isEmpty()) continue;
            assign(category.get());
        }
    }

    public void assign(Category category) {
        @Language("postgresql")
        var insert = """
                INSERT INTO tag_category(tag_id, category_id) VALUES(?,?)""";
        builder().query(insert)
                .parameter(stmt -> stmt.setInt(meta.tag().id()).setInt(category.id()))
                .insert()
                .sendSync();
    }

    public void clear() {
        builder().query("DELETE FROM tag_category where tag_id = ?")
                .parameter(stmt -> stmt.setInt(meta.tag().id()))
                .delete()
                .sendSync();
    }

    public List<Category> all() {
         @Language("postgresql")
          var select = """
              SELECT id, category
              FROM tag_category
                       LEFT JOIN category c on c.id = tag_category.category_id
              WHERE tag_id = ?""";
         return builder(Category.class)
                 .query(select)
                 .parameter(stmt -> stmt.setInt(meta.tag().id()))
                 .readRow(Category::build)
                 .allSync();
    }
}
