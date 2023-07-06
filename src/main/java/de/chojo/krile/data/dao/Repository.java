package de.chojo.krile.data.dao;

import de.chojo.krile.data.access.Authors;
import de.chojo.krile.data.access.Categories;
import de.chojo.krile.data.dao.repository.Data;
import de.chojo.krile.data.dao.repository.Meta;
import de.chojo.krile.data.dao.repository.Tags;
import de.chojo.krile.tagimport.repo.RawRepository;
import de.chojo.sadu.wrapper.util.Row;

import java.sql.SQLException;

public class Repository {
    private final int id;
    private final String url;
    private final Identifier identifier;
    private final Data data;
    private final Meta meta;
    private final Tags tags;

    public Repository(int id, String url, String identifier, String directory, Categories categories, Authors authors) {
        this.id = id;
        this.url = url;
        this.identifier = Identifier.parse(identifier).get();
        data = new Data(this);
        meta = new Meta(this, categories);
        tags = new Tags(this, categories, authors);
    }

    public static Repository build(Row row, Categories categories, Authors authors) throws SQLException {
        return new Repository(row.getInt("id"), row.getString("url"), row.getString("identifier"), row.getString("directory"), categories, authors);
    }

    public void update(RawRepository repository) {
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

    public Identifier identifier() {
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
