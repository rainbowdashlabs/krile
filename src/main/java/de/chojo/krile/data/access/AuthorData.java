package de.chojo.krile.data.access;

import de.chojo.krile.data.dao.Author;
import de.chojo.krile.tagimport.tag.entities.RawAuthor;
import org.intellij.lang.annotations.Language;

import java.util.Optional;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class AuthorData {
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
        return builder(Author.class)
                .query(query)
                .parameter(stmt -> stmt.setString(author.name()).setString(author.mail()))
                .readRow(Author::build)
                .firstSync();
    }

    public Optional<Author> get(RawAuthor author) {
        @Language("postgresql")
        var query = """
                SELECT id, name, mail FROM author WHERE name = ? AND mail = ?""";
        return builder(Author.class)
                .query(query)
                .parameter(stmt -> stmt.setString(author.name()).setString(author.mail()))
                .readRow(Author::build)
                .firstSync();
    }
}
