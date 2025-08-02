/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao.tagguild;

import de.chojo.jdautil.configuration.Configuration;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.data.access.AuthorData;
import de.chojo.krile.data.access.CategoryData;
import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.data.dao.TagGuild;
import de.chojo.sadu.mapper.wrapper.Row;
import org.intellij.lang.annotations.Language;

import java.sql.SQLException;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class GuildRepository extends Repository {
    private final TagGuild guild;
    private final int priority;

    public GuildRepository(TagGuild guild, int priority, int id, String url, String identifier, String directory, Configuration<ConfigFile> configuration, CategoryData categories, AuthorData authors) {
        super(id, url, identifier, directory, configuration, categories, authors);
        this.guild = guild;
        this.priority = priority;
    }

    public static GuildRepository build(Row row, TagGuild guild, Configuration<ConfigFile> configuration, CategoryData categories, AuthorData authors) throws SQLException {
        return new GuildRepository(guild,
                row.getInt("priority"),
                row.getInt("id"),
                row.getString("url"),
                row.getString("identifier"),
                row.getString("directory"),
                configuration,
                categories,
                authors);
    }

    /**
     * Returns the priority of the repository.
     *
     * @return the priority value of the repository.
     */
    public int priority() {
        return priority;
    }

    /**
     * Unsubscribes the guild from the repository.
     *
     * @return {@code true} if the subscription was successfully unsubscribed, {@code false} otherwise.
     */
    public boolean unsubscribe() {
        @Language("postgresql")
        var delete = """
                DELETE FROM guild_repository WHERE guild_id = ? AND repository_id = ?""";
        return query(delete)
                .single(call().bind(guild.id()).bind(id()))
                .delete()
                .changed();
    }
}
