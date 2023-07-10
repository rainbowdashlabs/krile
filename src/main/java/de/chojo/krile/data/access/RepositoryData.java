/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.access;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.data.dao.Identifier;
import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.data.util.CompletedCategory;
import de.chojo.krile.data.util.RepositoryFilter;
import de.chojo.krile.tagimport.exception.ImportException;
import de.chojo.krile.tagimport.exception.ParsingException;
import de.chojo.krile.tagimport.repo.RawRepository;
import de.chojo.krile.tagimport.repo.RepoConfig;
import de.chojo.sadu.wrapper.util.Row;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;
import static org.slf4j.LoggerFactory.getLogger;

public class RepositoryData {
    private final Configuration<ConfigFile> configuration;
    private final CategoryData categories;
    private final AuthorData authors;
    private static final Logger log = getLogger(RepositoryData.class);

    public RepositoryData(Configuration<ConfigFile> configuration, CategoryData categories, AuthorData authors) {
        this.configuration = configuration;
        this.categories = categories;
        this.authors = authors;
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

    public Optional<Repository> getOrCreateByIdentifier(Identifier identifier, EventContext context, IDeferrableCallback callback) throws ImportException, ParsingException {
        Optional<Repository> repository = byIdentifier(identifier);
        if (repository.isPresent()) return repository;
        log.info("Starting integration process for {}", identifier);
        callback.getHook().editOriginal(context.guildLocale("command.add.message.unknown")).queue();
        RawRepository rawRepository = RawRepository.remote(configuration, identifier);
        log.info("Cloned repository");
        callback.getHook().editOriginal(context.guildLocale("command.add.message.parsing")).queue();
        log.info("Creating database entry");
        Optional<Repository> repo = create(rawRepository);
        callback.getHook().editOriginal(context.guildLocale("command.add.message.parsing")).queue();
        log.info("Inserting data");
        repo.get().update(rawRepository);
        log.info("Integration done.");
        return repo;
    }

    public Optional<Repository> create(RawRepository repository) throws ParsingException {
        Identifier id = repository.identifier();
        RepoConfig conf = repository.configuration();
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
                        .setString(conf.directory()))
                .readRow(this::buildRepository)
                .firstSync();
    }

    public List<String> completeName(String value) {
        return completeBase("name", value);
    }

    public List<String> completeRepo(String value) {
        return completeBase("repo", value);
    }

    public List<String> completePath(String value) {
        return completeBase("path", value);
    }

    public List<String> completeIdentifier(String value) {
        return completeBase("identifier", value);
    }

    public List<String> completeLanguage(String value) {
        return completeMeta("language", value);
    }

    public List<Repository> leastUpdated(int check, int limit) {
        @Language("postgresql")
        var select = """
                SELECT id, url, identifier, directory
                FROM repository r
                         LEFT JOIN repository_data rd ON r.id = rd.repository_id
                WHERE checked < now() AT TIME ZONE 'UTC' - (?::TEXT || ' MINUTES')::INTERVAL
                ORDER BY checked
                LIMIT ?""";
        return builder(Repository.class)
                .query(select)
                .parameter(stmt -> stmt.setInt(check).setInt(limit))
                .readRow(this::buildRepository)
                .allSync();
    }

    public Optional<Repository> random() {
        @Language("postgresql")
        var select = """
                SELECT id, url, identifier, directory
                FROM repository
                ORDER BY random()
                LIMIT 1""";

        return builder(Repository.class)
                .query(select)
                .emptyParams()
                .readRow(this::buildRepository)
                .firstSync();
    }

    public List<Repository> search(RepositoryFilter filter) {
        @Language("postgresql")
        var select = """
                WITH categories
                         AS (SELECT DISTINCT repository_id
                             FROM repository_category
                             WHERE category_id = ?
                                OR ? IS NULL)
                SELECT r.id, url, identifier, directory
                FROM categories c
                         LEFT JOIN repository r ON c.repository_id = r.id
                         LEFT JOIN repository_meta rm ON r.id = rm.repository_id
                         LEFT JOIN repo_stats s ON r.id = s.id
                WHERE (platform ILIKE ? OR ? IS NULL)
                  AND (r.name ILIKE ? OR ? IS NULL) -- username
                  AND (repo ILIKE ? OR ? IS NULL)
                  AND (rm.name ILIKE ? OR ? IS NULL) -- repository name
                  AND (rm.language ILIKE ? OR ? IS NULL)
                  AND (tags >= ? OR ? IS NULL)
                  AND rm.public
                LIMIT 50""";
        return builder(Repository.class)
                .query(select)
                .parameter(stmt -> stmt
                        .setInt(filter.category()).setInt(filter.category())
                        .setString(filter.platform()).setString(filter.platform())
                        .setString(filter.user()).setString(filter.user())
                        .setString(filter.repo()).setString(filter.repo())
                        .setString(filter.name()).setString(filter.name())
                        .setString(filter.language()).setString(filter.language())
                        .setInt(filter.tags()).setInt(filter.tags())
                ).readRow(this::buildRepository)
                .allSync();
    }

    public List<CompletedCategory> completeCategories(String value) {
        @Language("postgresql")
        var select = """
                SELECT DISTINCT id, category
                FROM repository_category rc
                         LEFT JOIN category c ON rc.category_id = c.id
                         LEFT JOIN repository_meta rm ON rc.repository_id = rm.repository_id
                WHERE rm.public AND category ILIKE ('%' || ? || '%')
                LIMIT 25""";

        return builder(CompletedCategory.class)
                .query(select)
                .parameter(stmt -> stmt.setString(value))
                .readRow(row -> new CompletedCategory(row.getInt("id"), row.getString("category")))
                .allSync();
    }

    private Repository buildRepository(Row row) throws SQLException {
        return Repository.build(row, configuration, categories, authors);
    }

    private List<String> completeBase(String column, String value) {
        @Language("postgresql")
        var select = """
                SELECT r.%s
                FROM repository r
                         LEFT JOIN repository_meta rm ON r.id = rm.repository_id
                WHERE r.%s ILIKE '%%' || ? || '%%'
                  AND rm.public
                LIMIT 24
                """;
        return complete(select, column, value);
    }

    private List<String> completeMeta(String column, String value) {
        @Language("postgresql")
        var select = """
                SELECT DISTINCT rm.%s
                FROM repository r
                         LEFT JOIN repository_meta rm ON r.id = rm.repository_id
                WHERE rm.%s ILIKE '%%' || ? || '%%'
                  AND rm.public
                LIMIT 24
                """;
        return complete(select, column, value);
    }

    private List<String> complete(String query, String column, String value) {
        List<String> result = new ArrayList<>();
        if (!value.isBlank()) result.add(value);
        List<String> name = builder(String.class)
                .query(query, column, column)
                .parameter(stmt -> stmt.setString(value))
                .readRow(row -> row.getString(column))
                .allSync();
        result.addAll(name);
        return result;
    }
}
