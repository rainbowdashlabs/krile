/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.access;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.krile.data.dao.Author;
import de.chojo.krile.tagimport.tag.entities.RawAuthor;
import org.intellij.lang.annotations.Language;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class AuthorData {
    private final Cache<Integer, Author> authorCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();

    public Optional<Author> getOrCreate(RawAuthor author) {
        return get(author).or(() -> create(author));
    }

    /**
     * Create a new author in the database.
     *
     * @param author the raw author object containing the name and mail of the author
     * @return an Optional object containing the created Author, if successful; otherwise empty
     */
    public Optional<Author> create(RawAuthor author) {
        @Language("postgresql")
        var query = """
                INSERT INTO author(name, mail)
                VALUES (?, ?)
                ON CONFLICT(name, mail)
                    DO NOTHING
                RETURNING id,name, mail""";
        return cache(builder(Author.class)
                .query(query)
                .parameter(stmt -> stmt.setString(author.name()).setString(author.mail()))
                .readRow(Author::build)
                .firstSync());
    }

    /**
     * Retrieve an existing author from the database based on their name and mail.
     *
     * @param author the raw author object containing the name and mail of the author
     * @return an Optional object containing the retrieved Author if it exists; otherwise empty
     */
    public Optional<Author> get(RawAuthor author) {
        @Language("postgresql")
        var query = """
                SELECT id, name, mail FROM author WHERE name = ? AND mail = ?""";
        return cache(builder(Author.class)
                .query(query)
                .parameter(stmt -> stmt.setString(author.name()).setString(author.mail()))
                .readRow(Author::build)
                .firstSync());
    }

    /**
     * Retrieve an existing author from the cache based on their ID. If the author is not found in the cache,
     * it will be retrieved from the database and then cached.
     *
     * @param id the ID of the author
     * @return an Optional object containing the retrieved Author if it exists in the cache or database; otherwise empty
     */
    public Optional<Author> get(int id) {
        Author author = authorCache.getIfPresent(id);
        if (author == null) retrieveById(id).ifPresent(this::cache);
        return Optional.ofNullable(author);
    }

    private Author cache(Author author) {
        authorCache.put(author.id(), author);
        return author;
    }

    private Optional<Author> cache(Optional<Author> author) {
        author.ifPresent(this::cache);
        return author;
    }

    private Optional<Author> retrieveById(int id) {
        @Language("postgresql")
        var query = """
                SELECT id, name, mail FROM author WHERE id = ?""";
        return builder(Author.class)
                .query(query)
                .parameter(stmt -> stmt.setInt(id))
                .readRow(Author::build)
                .firstSync();
    }
}
