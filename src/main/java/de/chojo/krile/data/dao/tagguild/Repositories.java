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
