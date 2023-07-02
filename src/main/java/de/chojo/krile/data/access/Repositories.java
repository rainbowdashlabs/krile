package de.chojo.krile.data.access;

import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.tagimport.repo.RawTagRepository;
import de.chojo.sadu.wrapper.util.Row;
import org.intellij.lang.annotations.Language;

import java.sql.SQLException;
import java.util.Optional;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class Repositories {
    private final Categories categories;
    private final Authors authors;

    public Repositories(Categories categories, Authors authors) {
        this.categories = categories;
        this.authors = authors;
    }

    public Optional<Repository> getOrCreate(RawTagRepository repository) {
        return byIdentifier(repository.identifier()).or(() -> create(repository));
    }

    public Optional<Repository> byIdentifier(String identifier) {
        @Language("postgresql")
        var query = """
                SELECT id, url, identifier FROM repository WHERE identifier = ?""";
        return builder(Repository.class)
                .query(query)
                .parameter(stmt -> stmt.setString(identifier))
                .readRow(this::buildRepository)
                .firstSync();
    }

    public Optional<Repository> byId(int id) {
        @Language("postgresql")
        var query = """
                SELECT id, url, identifier FROM repository WHERE id = ?""";
        return builder(Repository.class)
                .query(query)
                .parameter(stmt -> stmt.setInt(id))
                .readRow(this::buildRepository)
                .firstSync();
    }

    public Optional<Repository> create(RawTagRepository repository) {
        @Language("postgresql")
        var query = """
                INSERT INTO repository(url, identifier) VALUES(?, ?)
                    ON CONFLICT(identifier)
                    DO NOTHING
                RETURNING id, url, identifier;
                """;
        return builder(Repository.class)
                .query(query)
                .parameter(stmt -> stmt.setString(repository.url()).setString(repository.identifier()))
                .readRow(this::buildRepository)
                .firstSync();
    }

    private Repository buildRepository(Row row) throws SQLException {
        return Repository.build(row, categories, authors);
    }
}
