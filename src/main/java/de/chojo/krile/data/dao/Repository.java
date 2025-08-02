/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao;

import de.chojo.jdautil.configuration.Configuration;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.configuration.elements.RepositoryLocation;
import de.chojo.krile.data.access.AuthorData;
import de.chojo.krile.data.access.CategoryData;
import de.chojo.krile.data.dao.repository.Data;
import de.chojo.krile.data.dao.repository.Meta;
import de.chojo.krile.data.dao.repository.RepositoryMeta;
import de.chojo.krile.data.dao.repository.Tags;
import de.chojo.krile.tagimport.exception.ImportException;
import de.chojo.krile.tagimport.exception.ParsingException;
import de.chojo.krile.tagimport.repo.RawRepository;
import de.chojo.sadu.mapper.wrapper.Row;
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

    public Repository(int id, String url, String identifier, String directory, Configuration<ConfigFile> configuration, CategoryData categories, AuthorData authors) {
        this.id = id;
        this.url = url;
        this.identifier = Identifier.parse(identifier).get();
        this.directory = directory;
        this.configuration = configuration;
        data = new Data(this);
        meta = new Meta(this, categories);
        tags = new Tags(this, categories, authors);
    }

    public static Repository build(Row row, Configuration<ConfigFile> configuration, CategoryData categories, AuthorData authors) throws SQLException {
        return new Repository(row.getInt("id"), row.getString("url"), row.getString("identifier"), row.getString("directory"), configuration, categories, authors);
    }

    /**
     * Updates the repository by calling the update methods for meta, data, and tags.
     *
     * @param repository the RawRepository to be updated
     * @throws ParsingException if there is an error in parsing the repository data
     * @throws ImportException  if there is an error in importing the updated data
     */
    public void update(RawRepository repository) throws ParsingException, ImportException {
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

    /**
     * Generates a directory link for the given path by appending the directory, identifier path, and
     * repositoryLocation dir path.
     *
     * @param path the path to generate a directory link for
     * @return the generated directory link
     */
    public String directoryLink(String path) {
        if (directory != null && !directory.isBlank()) path = directory + "/" + path;
        if (identifier.path() != null) path = identifier.path() + "/" + path;
        RepositoryLocation repositoryLocation = configuration.config().repositories().find(identifier).get();
        return repositoryLocation.dirPath(identifier.user(), identifier.repo(), data.get().branch(), path);
    }

    /**
     * Generates a file link for the given path by appending the directory, identifier path, and
     * repositoryLocation file path.
     *
     * @param path the path to generate a file link for
     * @return the generated file link
     */
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

    /**
     * Generates an embed message with information about a repository.
     *
     * @param context the event context
     * @return the generated embed message
     */
    public MessageEmbed infoEmbed(EventContext context) {
        List<String> categories = meta.categories().all().stream().map(Category::name).toList();
        RepositoryMeta meta = this.meta.get();
        Data.RepositoryData data = this.data.get();
        EmbedBuilder builder = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setAuthor(identifier.toString())
                .setTitle(requireNonNullElse(meta.name(), identifier.name()), link());
        if (meta.description() != null) builder.setDescription(meta.description());
        if (meta.language() != null) builder.addField("words.language", meta.language(), true);
        builder.addField("words.public", (meta.visible() ? "words.yes" : "words.no"), true);
        if (!categories.isEmpty()) builder.addField("words.categories", String.join(", ", categories), true);
        builder.addField("words.tags", String.valueOf(tags.count()), true);
        builder.addBlankField(false);
        builder.addField("words.status", "%s / %s".formatted(data.commit().substring(0, 8), data.branch()), true);
        builder.addField("embeds.repository.last.checked", TimeFormat.RELATIVE.format(data.checked()), true);
        builder.addField("embeds.repository.last.update", TimeFormat.RELATIVE.format(data.updated()), true);
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

    /**
     * Marks the repository as checked.
     * The checked time will be updated to the current time.
     */
    public void checked() {
        data.checked();
    }

    /**
     * Updates the repository status to indicate a failed update with the given reason.
     *
     * @param reason the reason for the update failure
     */
    public void updateFailed(String reason) {
        data.updateFailed(reason);
    }
}
