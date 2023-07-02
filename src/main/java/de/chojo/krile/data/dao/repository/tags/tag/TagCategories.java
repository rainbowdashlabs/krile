package de.chojo.krile.data.dao.repository.tags.tag;

import de.chojo.krile.data.access.Categories;
import de.chojo.krile.data.dao.Category;
import de.chojo.krile.tagimport.tag.RawTag;
import org.intellij.lang.annotations.Language;

import java.util.Optional;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class TagCategories {
    private final TagMeta meta;
    private final Categories categories;

    public TagCategories(TagMeta meta, Categories categories) {
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
}
