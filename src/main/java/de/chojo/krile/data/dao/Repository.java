package de.chojo.krile.data.dao;

import de.chojo.krile.data.access.Authors;
import de.chojo.krile.data.access.Categories;
import de.chojo.krile.data.dao.repository.Data;
import de.chojo.krile.data.dao.repository.Meta;
import de.chojo.krile.data.dao.repository.Tags;
import de.chojo.krile.tagimport.repo.RawTagRepository;
import de.chojo.sadu.wrapper.util.Row;

import java.sql.SQLException;

public final class Repository {
    private final int id;
    private final String url;
    private final String identifier;
    private final Data data;
    private final Meta meta;
    private final Tags tags;

    public Repository(int id, String url, String identifier, Categories categories, Authors authors) {
        this.id = id;
        this.url = url;
        this.identifier = identifier;
        data = new Data(this);
        meta = new Meta(this, categories);
        tags = new Tags(this, categories, authors);
    }

    public static Repository build(Row row, Categories categories, Authors authors) throws SQLException {
        return new Repository(row.getInt("id"), row.getString("url"), row.getString("identifier"), categories, authors);
    }

    public void update(RawTagRepository repository) {
        meta.update(repository);
        data.update(repository);
        tags.update(repository);
    }


    public int id() {
        return id;
    }

    public String url() {
        return url;
    }

    public String identifier() {
        return identifier;
    }

    public Data data() {
        return data;
    }

    public Meta meta() {
        return meta;
    }

    public Tags tags() {
        return tags;
    }
}
