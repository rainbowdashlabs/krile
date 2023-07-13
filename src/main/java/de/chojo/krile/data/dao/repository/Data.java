/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao.repository;

import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.tagimport.exception.ImportException;
import de.chojo.krile.tagimport.repo.RawRepository;
import org.intellij.lang.annotations.Language;

import java.time.Instant;
import java.time.ZoneOffset;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class Data {
    private final Repository repository;

    public Data(Repository repository) {
        this.repository = repository;
    }

    /**
     * Updates the given repository with the current commit and branch information.
     *
     * @param repository the repository to update
     * @throws ImportException if an error occurs during the update process
     */
    public void update(RawRepository repository) throws ImportException {
        String currentCommit = repository.currentCommit();
        String currentBranch = repository.currentBranch();

        @Language("postgresql")
        var insert = """
                INSERT INTO repository_data(repository_id, updated, checked, commit, branch)
                VALUES (?, now() AT TIME ZONE 'UTC', now() AT TIME ZONE 'UTC', ?, ?)
                ON CONFLICT(repository_id)
                    DO UPDATE
                    SET updated = now() AT TIME ZONE 'UTC',
                        checked = now() AT TIME ZONE 'UTC',
                        commit  = excluded.commit,
                        branch = excluded.branch,
                        status = NULL""";
        builder()
                .query(insert)
                .parameter(stmt -> stmt.setInt(this.repository.id()).setString(currentCommit).setString(currentBranch))
                .insert()
                .sendSync();
    }

    /**
     * Retrieves the most recent information about the repository.
     *
     * @return the most recent repository data
     */
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

    /**
     * Updates the 'checked' timestamp and clears the 'status' for the repository.
     */
    public void checked() {
        @Language("postgresql")
        var update = """
                UPDATE repository_data
                SET checked = now() AT TIME ZONE 'UTC', status = NULL
                WHERE repository_id = ?""";

        builder()
                .query(update)
                .parameter(stmt -> stmt.setInt(repository.id()))
                .update()
                .sendSync();
    }

    /**
     * Updates the 'status' of the repository with the given reason.
     *
     * @param reason The reason for the update failure.
     */
    public void updateFailed(String reason) {
        @Language("postgresql")
        var update = """
                UPDATE repository_data SET status = ? WHERE repository_id = ?""";
        builder()
                .query(update)
                .parameter(stmt -> stmt.setString(reason).setInt(repository.id()))
                .update()
                .sendSync();
    }

    public record RepositoryData(Instant updated, Instant checked, String commit, String branch) {
    }
}
