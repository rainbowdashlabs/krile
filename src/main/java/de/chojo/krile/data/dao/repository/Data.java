package de.chojo.krile.data.dao.repository;

import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.data.dao.RepositoryUpdateException;
import de.chojo.krile.tagimport.repo.RawRepository;
import org.intellij.lang.annotations.Language;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class Data {
    private final Repository repository;

    public Data(Repository repository) {
        this.repository = repository;
    }

    public void update(RawRepository repository) {
        String currentCommit;
        String currentBranch;
        try {
            currentCommit = repository.currentCommit();
            currentBranch = repository.currentBranch();
        } catch (IOException e) {
            throw new RepositoryUpdateException(repository, e);
        }

        @Language("postgresql")
        var insert = """
                INSERT INTO repository_data(repository_id, updated, checked, commit, branch)
                VALUES (?, now() at time zone 'UTC', now() at time zone 'UTC', ?, ?)
                ON CONFLICT(repository_id)
                    DO UPDATE
                    SET updated = now() at time zone 'UTC',
                        checked = now() at time zone 'UTC',
                        commit  = excluded.commit,
                        branch = excluded.branch""";
        builder()
                .query(insert)
                .parameter(stmt -> stmt.setInt(this.repository.id()).setString(currentCommit).setString(currentBranch))
                .insert()
                .sendSync();
    }

    public RepositoryData get() {
        @Language("postgresql")
        var select = """
                SELECT updated, checked, commit, branch
                FROM repository_data
                WHERE repository_id = ?""";
        return builder(RepositoryData.class)
                .query(select)
                .parameter(stmt -> stmt.setInt(repository.id()))
                .readRow(row -> new RepositoryData(
                        row.getLocalDateTime("updated").toInstant(ZoneOffset.UTC),
                        row.getLocalDateTime("checked").toInstant(ZoneOffset.UTC),
                        row.getString("commit"),
                        row.getString("branch")
                ))
                .firstSync()
                .get();

    }

    public record RepositoryData(Instant updated, Instant checked, String commit, String branch) {
    }
}
