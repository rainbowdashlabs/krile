package de.chojo.krile.data.dao;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.configuration.elements.RepositoryLocation;
import de.chojo.krile.data.access.Authors;
import de.chojo.krile.data.access.Categories;
import de.chojo.krile.data.dao.repository.Data;
import de.chojo.krile.data.dao.repository.Meta;
import de.chojo.krile.data.dao.repository.RepositoryMeta;
import de.chojo.krile.data.dao.repository.Tags;
import de.chojo.krile.tagimport.repo.RawRepository;
import de.chojo.sadu.wrapper.util.Row;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.sql.SQLException;
import java.util.List;

import static java.util.Objects.requireNonNullElse;

public class Repository {
    private final int id;
    private final String url;
    private final Identifier identifier;
    private final String directory;
    private final Configuration<ConfigFile> configuration;
    private final Data data;
    private final Meta meta;
    private final Tags tags;

    public Repository(int id, String url, String identifier, String directory, Configuration<ConfigFile> configuration, Categories categories, Authors authors) {
        this.id = id;
        this.url = url;
        this.identifier = Identifier.parse(identifier).get();
        this.directory = directory;
        this.configuration = configuration;
        data = new Data(this);
        meta = new Meta(this, categories);
        tags = new Tags(this, categories, authors);
    }

    public static Repository build(Row row, Configuration<ConfigFile> configuration, Categories categories, Authors authors) throws SQLException {
        return new Repository(row.getInt("id"), row.getString("url"), row.getString("identifier"), row.getString("directory"), configuration, categories, authors);
    }

    public void update(RawRepository repository) {
        meta.update(repository);
        data.update(repository);
        tags.update(repository);
    }


    public int id() {
        return id;
    }

    public String url() {
        return url;
    }

    public Identifier identifier() {
        return identifier;
    }

    public String directoryLink(String path) {
        if (directory != null && !directory.isBlank()) path = directory + "/" + path;
        if (identifier.path() != null) path = identifier.path() + "/" + path;
        RepositoryLocation repositoryLocation = configuration.config().repositories().find(identifier).get();
        return repositoryLocation.dirPath(identifier.user(), identifier.repo(), data.get().branch(), path);
    }

    public String fileLink(String path) {
        if (directory != null && !directory.isBlank()) path = directory + "/" + path;
        if (identifier.path() != null) path = identifier.path() + "/" + path;
        RepositoryLocation repositoryLocation = configuration.config().repositories().find(identifier).get();
        return repositoryLocation.filePath(identifier.user(), identifier.repo(), data.get().branch(), path);
    }

    public Data data() {
        return data;
    }

    public Meta meta() {
        return meta;
    }

    public Tags tags() {
        return tags;
    }

    public String link() {
        return directoryLink("");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Repository that = (Repository) o;

        return identifier.equals(that.identifier);
    }

    public MessageEmbed infoEmbed(EventContext context) {
        List<String> categories = meta.categories().all().stream().map(Category::name).toList();
        RepositoryMeta meta = this.meta.get();
        Data.RepositoryData data = this.data.get();
        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(identifier.toString())
                .setTitle(requireNonNullElse(meta.name(), identifier.name()), link());
        if (meta.description() != null) builder.setDescription(meta.description());
        if (meta.language() != null) builder.addField("Language", meta.language(), true);
        builder.addField("Public", (meta.visible() ? "yes" : "no"), true);
        if (!categories.isEmpty()) builder.addField("Categories", String.join(", ", categories), true);
        builder.addField("Tags", String.valueOf(tags.count()), true);
        builder.addBlankField(false);
        builder.addField("Status", "%s on %s".formatted(data.commit().substring(0, 8), data.branch()), true);
        builder.addField("Last Checked", TimeFormat.RELATIVE.format(data.checked()), true);
        builder.addField("Last Updated", TimeFormat.RELATIVE.format(data.updated()), true);
        // TODO add rankings and metrics c:
        return builder.build();
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public String toString() {
        return identifier.toString();
    }

    public void checked() {
        data.checked();
    }
}
