package de.chojo.krile.data.dao.repository;

import de.chojo.krile.data.access.CategoryData;
import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.tagimport.repo.RawRepository;
import de.chojo.krile.tagimport.repo.RepoConfig;
import org.intellij.lang.annotations.Language;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class Meta {
    private final Repository repository;
    private final RepositoryCategories categories;

    public Meta(Repository repository, CategoryData categories) {
        this.repository = repository;
        this.categories = new RepositoryCategories(this, categories);
    }

    public void update(RawRepository repository) {
        RepoConfig configuration = repository.configuration();
        @Language("postgresql")
        var insert = """
                INSERT INTO repository_meta(repository_id, name, description, public_repo, language)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(repository_id)
                    DO UPDATE
                    SET name        = excluded.name,
                        description = excluded.description,
                        public_repo = excluded.public_repo,
                        language    = excluded.language""";
        // Update meta
        builder()
                .query(insert)
                .parameter(stmt -> stmt.setInt(this.repository.id())
                        .setString(configuration.name())
                        .setString(configuration.description())
                        .setBoolean(configuration.publicRepo())
                        .setString(configuration.language()))
                .insert()
                .sendSync();

        categories.updateCategories(repository);
    }

    public RepositoryMeta get() {
        @Language("postgresql")
        var select = """
                SELECT name, description, public_repo, language, public FROm repository_meta WHERE repository_id = ?""";

        return builder(RepositoryMeta.class)
                .query(select)
                .parameter(stmt -> stmt.setInt(repository.id()))
                .readRow(row -> new RepositoryMeta(
                        row.getString("name"),
                        row.getString("description"),
                        row.getBoolean("public_repo"),
                        row.getBoolean("public"),
                        row.getString("language")))
                .firstSync()
                .get();
    }

    public Repository repository() {
        return repository;
    }

    public RepositoryCategories categories() {
        return categories;
    }
}
