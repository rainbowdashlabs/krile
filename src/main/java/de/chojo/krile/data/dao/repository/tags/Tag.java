package de.chojo.krile.data.dao.repository.tags;

import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.data.access.Authors;
import de.chojo.krile.data.access.Categories;
import de.chojo.krile.data.dao.Author;
import de.chojo.krile.data.dao.Category;
import de.chojo.krile.data.dao.Identifier;
import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.data.dao.repository.tags.tag.TagMeta;
import de.chojo.krile.data.dao.repository.tags.tag.tagmeta.FileMeta;
import de.chojo.krile.tagimport.tag.RawTag;
import de.chojo.sadu.types.PostgreSqlTypes;
import de.chojo.sadu.wrapper.util.Row;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.eclipse.jetty.util.UrlEncoded;
import org.intellij.lang.annotations.Language;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.List;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public final class Tag {
    private final int id;
    private final String tagId;
    private final String tag;
    private final List<String> text;
    private final Repository repository;
    private final TagMeta meta;

    public Tag(int id, String tagId, String tag, List<String> text, Repository repository, Categories categories, Authors authors) {
        this.id = id;
        this.tagId = tagId;
        this.tag = tag;
        this.text = text;
        this.repository = repository;
        this.meta = new TagMeta(this, categories, authors);
    }

    public static Tag build(Row row, Repository repository, Categories categories, Authors authors) throws SQLException {
        return new Tag(row.getInt("id"), row.getString("tag_id"), row.getString("tag"), row.getList("content"), repository, categories, authors);
    }

    public Repository repository() {
        return repository;
    }

    public boolean delete() {
        return builder()
                .query("DELETE FROM tag WHERE id = ?")
                .parameter(stmt -> stmt.setInt(id()))
                .delete()
                .sendSync()
                .changed();
    }

    public int id() {
        return id;
    }

    public String tagId() {
        return tagId;
    }

    public String tag() {
        return tag;
    }

    public String text() {
        return text.get(0);
    }

    public List<String> paged() {
        return text;
    }

    public boolean isPaged() {
        return text.size() != 1;
    }

    public TagMeta meta() {
        return meta;
    }

    public String link() {
        // TODO proper string encoding. Something which does not replace space with a +
        return repository.fileLink(URLEncoder.encode(meta.fileMeta().fileName(), Charset.defaultCharset()).replace("+", "%20"));
    }

    public void update(RawTag raw) {
        @Language("postgresql")
        var insert = """
                UPDATE tag SET content = ? WHERE id = ?""";

        builder()
                .query(insert)
                .parameter(stmt -> stmt.setArray(raw.splitText(), PostgreSqlTypes.TEXT).setInt(id()))
                .update()
                .sendSync();
        meta.update(raw);
    }

    public MessageEmbed infoEmbed(EventContext context) {

        Identifier identifier = repository.identifier();
        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor("ID: %s Repo: %s".formatted(tagId, identifier), link())
                .setTitle(tag);
        List<String> aliases = meta.aliases().all();
        if (!aliases.isEmpty()) builder.addField("Aliases", String.join(", ", aliases), true);
        List<String> categories = meta.categories().all().stream().map(Category::name).toList();
        if (!categories.isEmpty()) builder.addField("Categories", String.join(", ", categories), true);
        List<String> authors = meta.tagAuthors().all().stream().map(Author::name).distinct().toList();
        if (!authors.isEmpty()) builder.addField("Authors", String.join(", ", authors), true);

        FileMeta fileMeta = meta.fileMeta();
        var created = fileMeta.created();
        var modified = fileMeta.modified();
        builder.addField("Created", "%s by %s".formatted(TimeFormat.RELATIVE.format(created.when()), created.who().name()), true);
        builder.addField("Modified", "%s by %s".formatted(TimeFormat.RELATIVE.format(modified.when()), modified.who().name()), true);
        builder.addField("", "Links: [File](%s) | [Repository](%s)".formatted(link(), repository.link()), false);
        return builder.build();
    }
}
