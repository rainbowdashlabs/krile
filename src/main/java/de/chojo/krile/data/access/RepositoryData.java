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
import java.util.ArrayList;
import java.util.List;
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
                SELECT id, url, identifier, directory FROM repository WHERE identifier = ?""";
        return builder(Repository.class)
                .query(query)
                .parameter(stmt -> stmt.setString(identifier))
                .readRow(this::buildRepository)
                .firstSync();
    }

    public Optional<Repository> byId(int id) {
        @Language("postgresql")
        var query = """
                SELECT id, url, identifier, directory FROM repository WHERE id = ?""";
        return builder(Repository.class)
                .query(query)
                .parameter(stmt -> stmt.setInt(id))
                .readRow(this::buildRepository)
                .firstSync();
    }

    public Optional<Repository> byUrl(String url) {
        @Language("postgresql")
        var query = """
                SELECT id, url, identifier, directory FROM repository WHERE url = ?""";
        return builder(Repository.class)
                .query(query)
                .parameter(stmt -> stmt.setString(url))
                .readRow(this::buildRepository)
                .firstSync();
    }

    public Optional<Repository> byIdentifier(Identifier identifier) {
        @Language("postgresql")
        var query = """
                SELECT id, url, identifier, directory FROM repository WHERE identifier = ?""";
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
        Optional<Repository> repo = create(rawRepository);
        repo.get().update(rawRepository);

        return repo;
    }

    public Optional<Repository> create(RawRepository repository) {
        Identifier id = repository.identifier();
        @Language("postgresql")
        var query = """
                INSERT INTO repository(url, platform, name, repo, path, directory) VALUES(?, ?, ?, ?, ?, ?)
                    ON CONFLICT(platform, name, repo, coalesce(path, ''))
                    DO NOTHING
                RETURNING id, url, identifier, directory;
                """;
        return builder(Repository.class)
                .query(query)
                .parameter(stmt -> stmt.setString(repository.url())
                        .setString(id.platform())
                        .setString(id.user())
                        .setString(id.repo())
                        .setString(id.path())
                        .setString(repository.configuration().directory()))
                .readRow(this::buildRepository)
                .firstSync();
    }

    private Repository buildRepository(Row row) throws SQLException {
        return Repository.build(row, configuration, categories, authors);
    }

    public List<String> completeName(String value) {
        return complete("name", value);
    }

    public List<String> completeRepo(String value) {
        return complete("repo", value);
    }

    public List<String> completePath(String value) {
        return complete("path", value);
    }

    private List<String> complete(String column, String value) {
        @Language("postgresql")
        var select = """
                SELECT r.%s
                FROM repository r
                         LEFT JOIN repository_meta rm on r.id = rm.repository_id
                WHERE r.%s ILIKE '%%' || ? || '%%'
                  AND rm.public
                LIMIT 24
                """;
        List<String> result = new ArrayList<>();
        if (!value.isBlank()) result.add(value);
        List<String> name = builder(String.class)
                .query(select, column, column)
                .parameter(stmt -> stmt.setString(value))
                .readRow(row -> row.getString(column))
                .allSync();
        result.addAll(name);
        return result;
    }

    public List<String> completeIdentifier(String value) {
        return complete("identifier", value);
    }

    public List<Repository> leastUpdated(int check, int limit) {
         @Language("postgresql")
          var select = """
              SELECT id, url, identifier, directory
              FROM repository r
                       LEFT JOIN repository_data rd on r.id = rd.repository_id
              WHERE checked < now() at time zone 'UTC' - (?::TEXT || ' MINUTES')::INTERVAL
              ORDER BY checked
              LIMIT ?""";
         return builder(Repository.class)
                 .query(select)
                 .parameter(stmt -> stmt.setInt(check).setInt(limit))
                 .readRow(this::buildRepository)
                 .allSync();
    }
}
