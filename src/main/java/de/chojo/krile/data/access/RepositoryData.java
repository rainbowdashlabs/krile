package de.chojo.krile.data.access;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.data.dao.Identifier;
import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.tagimport.repo.RawRepository;
import de.chojo.sadu.wrapper.util.Row;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.intellij.lang.annotations.Language;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class RepositoryData {
    private final Configuration<ConfigFile> configuration;
    private final Categories categories;
    private final Authors authors;

    public RepositoryData(Configuration<ConfigFile> configuration, Categories categories, Authors authors) {
        this.configuration = configuration;
        this.categories = categories;
        this.authors = authors;
    }

    public Optional<Repository> getOrCreate(RawRepository repositoryLocation) {
        return byIdentifier(repositoryLocation.identifier()).or(() -> create(repositoryLocation));
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

    public Optional<Repository> byUrl(String url) {
        @Language("postgresql")
        var query = """
                SELECT id, url, identifier FROM repository WHERE url = ?""";
        return builder(Repository.class)
                .query(query)
                .parameter(stmt -> stmt.setString(url))
                .readRow(this::buildRepository)
                .firstSync();
    }
    public Optional<Repository> byIdentifier(Identifier identifier) {
        @Language("postgresql")
        var query = """
                SELECT id, url, identifier FROM repository WHERE identifier = ?""";
        return builder(Repository.class)
                .query(query)
                .parameter(stmt -> stmt.setString(identifier.toString()))
                .readRow(this::buildRepository)
                .firstSync();
    }

    public Optional<Repository> getOrCreateByIdentifier(Identifier identifier, IDeferrableCallback callback) throws GitAPIException, IOException {
        Optional<Repository> repository = byIdentifier(identifier);
        if (repository.isPresent()) return repository;
        callback.getHook().editOriginal("Unknown repository. Starting integration process");
        RawRepository rawRepository = RawRepository.remote(configuration, identifier);
        callback.getHook().editOriginal("Repository found. Parsing data.");
        create(rawRepository).get().update(rawRepository);

        return repository;
    }

    public Optional<Repository> create(RawRepository repository) {
        @Language("postgresql")
        var query = """
                INSERT INTO repository(url, identifier) VALUES(?, ?)
                    ON CONFLICT(identifier)
                    DO NOTHING
                RETURNING id, url, identifier;
                """;
        return builder(Repository.class)
                .query(query)
                .parameter(stmt -> stmt.setString(repository.url()).setString(repository.identifier().toString()))
                .readRow(this::buildRepository)
                .firstSync();
    }

    private Repository buildRepository(Row row) throws SQLException {
        return Repository.build(row, categories, authors);
    }
}
