package de.chojo.krile.data.dao.tagguild;

import de.chojo.krile.data.access.Authors;
import de.chojo.krile.data.access.Categories;
import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.data.dao.TagGuild;
import de.chojo.sadu.wrapper.util.Row;

import java.sql.SQLException;

public class GuildRepository extends Repository {
    private final TagGuild guild;
    private int prio;

    public GuildRepository(TagGuild guild, int prio, int id, String url, String identifier, Categories categories, Authors authors) {
        super(id, url, identifier, categories, authors);
        this.guild = guild;
        this.prio = prio;
    }

        public static GuildRepository build(Row row, TagGuild guild, Categories categories, Authors authors) throws SQLException {
        return new GuildRepository(guild, row.getInt("priority"), row.getInt("id"), row.getString("url"), row.getString("identifier"), categories, authors);
    }
}
