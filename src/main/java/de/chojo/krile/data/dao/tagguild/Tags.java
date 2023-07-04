package de.chojo.krile.data.dao.tagguild;

import de.chojo.krile.data.access.Authors;
import de.chojo.krile.data.access.Categories;
import de.chojo.krile.data.dao.TagGuild;
import de.chojo.sadu.wrapper.util.Row;
import org.intellij.lang.annotations.Language;

import java.sql.SQLException;
import java.util.List;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class Tags {
    private final TagGuild guild;

    public Tags(TagGuild guild, Categories categories, Authors authors) {
        this.guild = guild;
    }

    public List<CompletedTag> complete(String value) {
        @Language("postgresql")
        var select = """
                with ranked_tags as
                         (SELECT row_number() over (PARTITION BY tag ORDER BY gr.prio * rt.global_prio DESC) as rank,
                                 gr.repository_id,
                                 gr.prio                                                                     as repo_prio,
                                 global_prio,
                                 rt.id,
                                 tag,
                                 rt.prio                                                                     as tag_prio,
                                 r.identifier
                          FROM guild_repository gr
                                   LEFT JOIN repo_tags rt on gr.repository_id = rt.repository_id
                                   LEFT JOIN repository r on gr.repository_id = r.id
                          WHERE global_prio = 1
                            and tag ILIKE ('%' || ? || '%')
                            and gr.guild_id = ?)
                SELECT id, case when rank = 1 then tag else tag || ' (' || identifier || ')' end as name
                FROM ranked_tags;
                """;

        return builder(CompletedTag.class)
                .query(select)
                .parameter(stmt -> stmt.setString(value).setLong(guild.id()))
                .readRow(CompletedTag::build)
                .allSync();
    }

    public record CompletedTag(int id, String name) {
        public static CompletedTag build(Row row) throws SQLException {
            return new CompletedTag(row.getInt("id"), row.getString("name"));
        }
    }
}
