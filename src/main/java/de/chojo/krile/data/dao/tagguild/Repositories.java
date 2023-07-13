/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao.tagguild;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.data.access.AuthorData;
import de.chojo.krile.data.access.CategoryData;
import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.data.dao.TagGuild;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.Optional;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class Repositories {
    private final TagGuild guild;
    private final Configuration<ConfigFile> configuration;
    private final AuthorData authors;
    private final CategoryData categories;

    public Repositories(TagGuild guild, Configuration<ConfigFile> configuration, AuthorData authors, CategoryData categories) {
        this.guild = guild;
        this.configuration = configuration;
        this.authors = authors;
        this.categories = categories;
    }

    /**
     * Retrieves all guild repositories associated with the given guild.
     *
     * @return A list of GuildRepository objects representing the guild repositories.
     */
    public List<GuildRepository> all() {
        @Language("postgresql")
        var select = """
                SELECT priority, r.id, url, identifier, directory
                FROM guild_repository gr
                         LEFT JOIN repository r ON r.id = gr.repository_id
                WHERE guild_id = ?
                ORDER BY priority""";

        return builder(GuildRepository.class)
                .query(select)
                .parameter(stmt -> stmt.setLong(guild.guild().getIdLong()))
                .readRow(row -> GuildRepository.build(row, guild, configuration, categories, authors))
                .allSync();
    }

    /**
     * Retrieves a guild repository by its ID.
     *
     * @param id The ID of the guild repository to retrieve.
     * @return An Optional containing the GuildRepository object representing the guild repository,
     * or an empty Optional if no matching repository was found.
     */
    public Optional<GuildRepository> byId(int id) {
        @Language("postgresql")
        var select = """
                SELECT priority, r.id, url, identifier, directory
                FROM guild_repository gr
                         LEFT JOIN repository r ON r.id = gr.repository_id
                WHERE id = ?
                ORDER BY priority""";

        return builder(GuildRepository.class)
                .query(select)
                .parameter(stmt -> stmt.setInt(id))
                .readRow(row -> GuildRepository.build(row, guild, configuration, categories, authors))
                .firstSync();
    }

    /**
     * Adds a repository to the guild's repository list.
     *
     * @param repository The repository to add.
     */
    public void add(Repository repository) {
        @Language("postgresql")
        var insert = """
                INSERT INTO guild_repository(guild_id, repository_id)
                VALUES (?, ?)
                ON CONFLICT DO NOTHING""";

        builder()
                .query(insert)
                .parameter(stmt -> stmt.setLong(guild.id()).setInt(repository.id()))
                .insert()
                .sendSync();
    }

    /**
     * Retrieves a list of completed repositories based on the provided search value.
     *
     * @param value The search value to match against repository identifiers.
     * @return A list of completed repositories.
     */
    public List<CompletedRepository> complete(String value) {
        @Language("postgresql")
        var select = """
                SELECT id, identifier
                FROM guild_repository gr
                         LEFT JOIN repository r ON r.id = gr.repository_id
                WHERE guild_id = ?
                  AND identifier ILIKE '%' || ? || '%'
                ORDER BY priority DESC
                LIMIT 25""";

        return builder(CompletedRepository.class)
                .query(select)
                .parameter(stmt -> stmt.setLong(guild.id()).setString(value))
                .readRow(row -> new CompletedRepository(row.getInt("id"), row.getString("identifier")))
                .allSync();
    }

    /**
     * Retrieves the count of guild repositories.
     *
     * @return The count of guild repositories.
     */
    public int count() {
        @Language("postgresql")
        var select = """
                SELECT count(1) FROM guild_repository WHERE guild_id = ?""";
        return builder(Integer.class)
                .query(select)
                .parameter(stmt -> stmt.setLong(guild.id()))
                .map()
                .firstSync()
                .orElse(0);
    }

    /**
     * Retrieves a page of guild repositories.
     *
     * @param page     The page number.
     * @param pageSize The number of repositories per page.
     * @return A list of strings representing the repositories.
     */
    public List<String> page(int page, int pageSize) {
        @Language("postgresql")
        var select = """
                SELECT rm.name, identifier
                FROM guild_repository gr
                         LEFT JOIN repository_meta rm ON gr.repository_id = rm.repository_id
                         LEFT JOIN repository r ON gr.repository_id = r.id
                WHERE guild_id = ?
                LIMIT ? OFFSET ?""";

        return builder(String.class)
                .query(select)
                .parameter(stmt -> stmt.setLong(guild.id()).setInt(pageSize).setInt(page * pageSize))
                .readRow(row -> {
                    String name = row.getString("name");
                    String identifier = row.getString("identifier");
                    if (name == null) return "`%s`".formatted(identifier);
                    return "%s (`%s`)".formatted(name, identifier);
                })
                .allSync();
    }

    public record CompletedRepository(int id, String identifier) {

    }
}
