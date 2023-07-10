/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao.repository.tags;

import de.chojo.jdautil.localization.LocalizationContext;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.krile.data.access.AuthorData;
import de.chojo.krile.data.access.CategoryData;
import de.chojo.krile.data.dao.Author;
import de.chojo.krile.data.dao.Category;
import de.chojo.krile.data.dao.Identifier;
import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.data.dao.repository.tags.tag.Meta;
import de.chojo.krile.data.dao.repository.tags.tag.meta.FileMeta;
import de.chojo.krile.data.dao.repository.tags.tag.meta.TagType;
import de.chojo.krile.tagimport.tag.RawTag;
import de.chojo.sadu.types.PostgreSqlTypes;
import de.chojo.sadu.wrapper.util.Row;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;
import static org.slf4j.LoggerFactory.getLogger;

public final class Tag {
    private static final Logger log = getLogger(Tag.class);
    private final int id;
    private final String tagId;
    private String tag;
    private List<String> text;
    private final Repository repository;
    private final Meta meta;

    public Tag(int id, String tagId, String tag, List<String> text, Repository repository, CategoryData categories, AuthorData authors) {
        this.id = id;
        this.tagId = tagId;
        this.tag = tag;
        this.text = text;
        this.repository = repository;
        this.meta = new Meta(this, categories, authors);
    }

    public static Tag build(Row row, Repository repository, CategoryData categories, AuthorData authors) throws SQLException {
        return new Tag(
                row.getInt("id"),
                row.getString("tag_id"),
                row.getString("tag"),
                row.getList("content"),
                repository,
                categories,
                authors);
    }

    public Repository repository() {
        return repository;
    }

    public boolean delete() {
        log.info("Deleted tag {} from {}", tagId, repository);
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
        if (meta.tagMeta().type() == TagType.EMBED) {
            DataObject dataObject = DataObject.fromJson(text.get(0));
            if (dataObject.hasKey("content")) {
                return dataObject.getString("content");
            }
            return "";
        }
        return text.get(0);
    }

    public List<MessageEmbed> embeds() {
        List<MessageEmbed> embeds = new ArrayList<>();
        DataObject dataObject = DataObject.fromJson(text.get(0));
        if (dataObject.hasKey("embeds")) {
            var array = dataObject.getArray("embeds");
            for (int i = 0; i < array.length(); i++) {
                embeds.add(EmbedBuilder.fromData(array.getObject(i)).build());
            }
        } else {
            embeds.add(EmbedBuilder.fromData(dataObject).build());
        }
        return embeds;
    }

    public List<String> paged() {
        return text;
    }

    public boolean isPaged() {
        return text.size() != 1;
    }

    public Meta meta() {
        return meta;
    }

    public String link() {
        // TODO proper string encoding. Something which does not replace space with a +
        return repository.fileLink(URLEncoder.encode(meta.fileMeta().fileName(), Charset.defaultCharset()).replace("+", "%20"));
    }

    public void update(RawTag raw) {
        log.trace("Updating tag {} in {}", tagId, repository);
        @Language("postgresql")
        var insert = """
                UPDATE tag
                SET
                    content = ?,
                    tag = ?
                WHERE id = ?""";

        List<String> text = switch (raw.meta().type()) {
            case TEXT -> raw.splitText();
            case EMBED -> List.of(raw.text());
        };

        builder()
                .query(insert)
                .parameter(stmt -> stmt.setArray(text, PostgreSqlTypes.TEXT).setString(raw.meta().tag()).setInt(id()))
                .update()
                .sendSync();
        this.text = text;
        this.tag = raw.meta().tag();
        meta.update(raw);
    }

    public MessageEmbed infoEmbed(LocalizationContext context) {
        Identifier identifier = repository.identifier();
        EmbedBuilder builder = new LocalizedEmbedBuilder(context)
                .setAuthor("embeds.tag.author", link(), Replacement.create("id", tagId), Replacement.create("identifier", identifier))
                .setTitle(tag);
        List<String> aliases = meta.aliases().all();
        if (!aliases.isEmpty()) builder.addField("words.aliases", String.join(", ", aliases), true);
        List<String> categories = meta.categories().all().stream().map(Category::name).toList();
        if (!categories.isEmpty()) builder.addField("words.categories", String.join(", ", categories), true);
        List<String> authors = meta.tagAuthors().all().stream().map(Author::name).distinct().toList();
        if (!authors.isEmpty()) builder.addField("words.authors", String.join(", ", authors), true);

        FileMeta fileMeta = meta.fileMeta();
        var created = fileMeta.created();
        var modified = fileMeta.modified();
        builder.addField("words.created", ("%s $words.by$ %s").formatted(TimeFormat.RELATIVE.format(created.when()), created.who().name()), true);
        builder.addField("words.modified", "%s $words.by$ %s".formatted(TimeFormat.RELATIVE.format(modified.when()), modified.who().name()), true);
        builder.addField("", "$words.links$: [$words.file$](%s) | [$words.repository$](%s)".formatted(link(), repository.link()), false);
        return builder.build();
    }
}
