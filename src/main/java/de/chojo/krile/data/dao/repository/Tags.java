package de.chojo.krile.data.dao.repository;

import de.chojo.krile.data.access.Authors;
import de.chojo.krile.data.access.Categories;
import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.data.dao.RepositoryUpdateException;
import de.chojo.krile.data.dao.repository.tags.Tag;
import de.chojo.krile.tagimport.repo.RawRepository;
import de.chojo.krile.tagimport.tag.RawTag;
import de.chojo.sadu.types.PostgreSqlTypes;
import de.chojo.sadu.types.SqlType;
import de.chojo.sadu.wrapper.util.Row;
import org.intellij.lang.annotations.Language;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class Tags {
    private final Repository repository;
    private final Categories categories;
    private final Authors authors;

    public Tags(Repository repository, Categories categories, Authors authors) {
        this.repository = repository;
        this.categories = categories;
        this.authors = authors;
    }

    public List<Tag> all() {
        @Language("postgresql")
        var select = """
                SELECT repository_id, id, tag_id, tag, content FROM tag WHERE repository_id = ?""";
        return builder(Tag.class)
                .query(select)
                .parameter(stmt -> stmt.setInt(repository.id()))
                .readRow(this::buildTag)
                .allSync();
    }

    public void update(RawRepository repository) {
        List<RawTag> tags;
        try {
            tags = repository.tags();
        } catch (IOException e) {
            throw new RepositoryUpdateException(repository, "Could not read tags", e);
        }

        List<Tag> all = all();

        for (RawTag raw : tags) {
            Optional<Tag> optTag = byRawTag(raw).or(() -> create(raw));
            if (optTag.isEmpty()) continue;
            Tag tag = optTag.get();
            tag.update(raw);
            all.removeIf(t -> t.id() == tag.id());
        }

        // Delete all not updated tags.
        all.forEach(Tag::delete);
    }

    public Optional<Tag> create(RawTag tag) {
        @Language("postgresql")
        var insert = """
                INSERT INTO tag(repository_id, tag_id, tag, content) VALUES(?,?,?,?)
                ON CONFLICT(repository_id, tag_id) DO NOTHING
                RETURNING id, tag_id, tag, content""";
        return builder(Tag.class)
                .query(insert)
                .parameter(stmt -> stmt.setInt(repository.id()).setString(tag.meta().tag()).setString(tag.meta().tag()).setArray(tag.splitText(), PostgreSqlTypes.TEXT))
                .readRow(this::buildTag)
                .firstSync();
    }

    public Optional<Tag> byRawTag(RawTag tag) {
        return byTagId(tag.meta().id());
    }

    public Optional<Tag> byTagId(String tagId) {
        @Language("postgresql")
        var select = """
                SELECT repository_id, id, tag_id, tag, content
                FROM tag
                WHERE tag_id = ?
                  AND repository_id = ?""";
        return builder(Tag.class)
                .query(select)
                .parameter(stmt -> stmt.setString(tagId).setInt(repository.id()))
                .readRow(this::buildTag)
                .firstSync();
    }

    private Tag buildTag(Row row) throws SQLException {
        return Tag.build(row, repository, categories, authors);
    }

    public int count() {
        return builder(Integer.class)
                .query("SELECT count(1) FROM tag WHERE repository_id = ?")
                .parameter(stmt -> stmt.setInt(repository.id()))
                .readRow(row -> row.getInt("count"))
                .firstSync()
                .orElse(0);
    }
}
