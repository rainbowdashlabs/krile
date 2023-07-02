package de.chojo.krile.data.dao.repository.tags.tag;

import de.chojo.krile.data.access.Authors;
import de.chojo.krile.data.access.Categories;
import de.chojo.krile.data.dao.Author;
import de.chojo.krile.data.dao.repository.tags.Tag;
import de.chojo.krile.tagimport.tag.RawTag;
import de.chojo.krile.tagimport.tag.entities.FileMeta;
import org.intellij.lang.annotations.Language;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class TagMeta {
    private final Tag tag;
    private final TagCategories categories;
    private final TagAuthors tagAuthors;
    private final Authors authors;
    private final TagAliases aliases;

    public TagMeta(Tag tag,  Categories categories, Authors authors) {
        this.tag = tag;
        this.categories = new TagCategories(this, categories);
        this.tagAuthors = new TagAuthors(this, authors);
        this.authors = authors;
        this.aliases = new TagAliases(this);
    }

    public void update(RawTag raw) {
        categories.update(raw);
        tagAuthors.update(raw);
        aliases.update(raw);
         @Language("postgresql")
          var insert = """
              INSERT INTO tag_meta(tag_id, image, created, created_by, modified, modified_by)
              VALUES (?, ?, ?, ?, ?, ?)
              ON CONFLICT (tag_id)
                  DO UPDATE
                  SET image       = excluded.image,
                      created     = excluded.created,
                      created_by  = excluded.created_by,
                      modified    = excluded.modified,
                      modified_by = excluded.modified_by
              """;
        FileMeta fileMeta = raw.fileMeta();
        OffsetDateTime created = fileMeta.created().when().atOffset(ZoneOffset.UTC);
        OffsetDateTime modified = fileMeta.modified().when().atOffset(ZoneOffset.UTC);
        Author createdBy = authors.get(fileMeta.created().who()).get();
        Author modifiedBy = authors.get(fileMeta.modified().who()).get();

        builder()
                 .query(insert)
                 .parameter(stmt -> stmt.setString(raw.meta().image())
                         .setOffsetDateTime(created)
                         .setInt(createdBy.id())
                         .setOffsetDateTime(modified)
                         .setInt(modifiedBy.id()))
                 .insert()
                 .sendSync();
    }

    public Tag tag() {
        return tag;
    }
}
