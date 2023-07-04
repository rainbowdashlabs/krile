package de.chojo.krile.data.dao.repository;

import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.data.dao.RepositoryUpdateException;
import de.chojo.krile.tagimport.repo.RawRepository;
import org.intellij.lang.annotations.Language;

import java.io.IOException;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class Data {
    private final Repository repository;

    public Data(Repository repository) {
        this.repository = repository;
    }

        public void update(RawRepository repository) {
        String currentCommit;
        try {
            currentCommit = repository.currentCommit();
        } catch (IOException e) {
            throw new RepositoryUpdateException(repository, e);
        }

        @Language("postgresql")
        var insert = """
                INSERT INTO repository_data(repository_id, updated, checked, commit)
                VALUES (?, now(), now(), ?)
                ON CONFLICT(repository_id)
                    DO UPDATE
                    SET updated = now(),
                        checked = now(),
                        commit  = excluded.commit""";
        builder()
                .query(insert)
                .parameter(stmt -> stmt.setInt(this.repository.id()).setString(currentCommit))
                .insert()
                .sendSync();
    }

}
