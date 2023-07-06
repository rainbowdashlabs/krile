package de.chojo.krile.data.dao.tagguild;

import de.chojo.krile.data.access.Authors;
import de.chojo.krile.data.access.Categories;
import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.data.dao.TagGuild;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.Optional;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class Repositories {
    private final TagGuild guild;
    private final Authors authors;
    private final Categories categories;

    public Repositories(TagGuild guild, Authors authors, Categories categories) {
        this.guild = guild;
        this.authors = authors;
        this.categories = categories;
    }

    public List<GuildRepository> all() {
        @Language("postgresql")
        var select = """
                SELECT priority, r.id, url, identifier, directory
                FROM guild_repository gr
                         LEFT JOIN repository r on r.id = gr.repository_id
                WHERE guild_id = ?
                ORDER BY priority""";

        return builder(GuildRepository.class)
                .query(select)
                .parameter(stmt -> stmt.setLong(guild.guild().getIdLong()))
                .readRow(row -> GuildRepository.build(row, guild, categories, authors))
                .allSync();
    }

    public Optional<GuildRepository> byId(int id) {
        @Language("postgresql")
        var select = """
                SELECT priority, r.id, url, identifier, directory
                FROM guild_repository gr
                         LEFT JOIN repository r on r.id = gr.repository_id
                WHERE id = ?
                ORDER BY priority""";

        return builder(GuildRepository.class)
                .query(select)
                .parameter(stmt -> stmt.setInt(id))
                .readRow(row -> GuildRepository.build(row, guild, categories, authors))
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
                         left join repository r on r.id = gr.repository_id
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

    public record CompletedRepository(int id, String identifier) {

    }
}
