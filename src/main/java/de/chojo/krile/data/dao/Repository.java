package de.chojo.krile.data.dao;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.configuration.elements.RepositoryLocation;
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
    private final String directory;
    private final Configuration<ConfigFile> configuration;
    private final Data data;
    private final Meta meta;
    private final Tags tags;

    public Repository(int id, String url, String identifier, String directory, Configuration<ConfigFile> configuration, Categories categories, Authors authors) {
        this.id = id;
        this.url = url;
        this.identifier = Identifier.parse(identifier).get();
        this.directory = directory;
        this.configuration = configuration;
        data = new Data(this);
        meta = new Meta(this, categories);
        tags = new Tags(this, categories, authors);
    }

    public static Repository build(Row row, Configuration<ConfigFile> configuration, Categories categories, Authors authors) throws SQLException {
        return new Repository(row.getInt("id"), row.getString("url"), row.getString("identifier"), row.getString("directory"), configuration, categories, authors);
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

    public String directoryLink(String path) {
        if (directory != null && !directory.isBlank()) path = directory + "/" + path;
        if (identifier.path() != null) path = identifier.path() + "/" + path;
        RepositoryLocation repositoryLocation = configuration.config().repositories().find(identifier).get();
        return repositoryLocation.dirPath(identifier.user(), identifier.repo(), data.get().branch(), path);
    }

    public String fileLink(String path) {
        if (directory != null && !directory.isBlank()) path = directory + "/" + path;
        if (identifier.path() != null) path = identifier.path() + "/" + path;
        RepositoryLocation repositoryLocation = configuration.config().repositories().find(identifier).get();
        return repositoryLocation.filePath(identifier.user(), identifier.repo(), data.get().branch(), path);
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

    public String link() {
        return directoryLink("");
    }
}
