package de.chojo.krile.data.dao.tagguild;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.data.access.AuthorData;
import de.chojo.krile.data.access.CategoryData;
import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.data.dao.TagGuild;
import de.chojo.sadu.wrapper.util.Row;
import org.intellij.lang.annotations.Language;

import java.sql.SQLException;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class GuildRepository extends Repository {
    private final TagGuild guild;
    private int priority;

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

    public int priority() {
        return priority;
    }

    public boolean unsubscribe() {
        @Language("postgresql")
        var delete = """
                DELETE FROM guild_repository WHERE guild_id = ? AND repository_id = ?""";
        return builder()
                .query(delete)
                .parameter(stmt -> stmt.setLong(guild.id()).setInt(id()))
                .delete()
                .sendSync()
                .changed();
    }
}
