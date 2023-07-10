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
