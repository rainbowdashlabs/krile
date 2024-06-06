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
import org.slf4j.Logger;

import java.time.Instant;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;
import static org.slf4j.LoggerFactory.getLogger;

public class Data {
    private final Repository repository;
    private static final Logger log = getLogger(Data.class);

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
        query(insert)
                .single(call().bind(this.repository.id()).bind(currentCommit).bind(currentBranch))
                .insert();
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
        return query(select)
                .single(call().bind(repository.id()))
                .map(row -> new RepositoryData(
                        row.get("updated", INSTANT_TIMESTAMP),
                        row.get("checked", INSTANT_TIMESTAMP),
                        row.getString("commit"),
                        row.getString("branch")
                ))
                .first()
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
        log.debug("Checked {} for updates", repository.identifier());

        query(update)
                .single(call().bind(repository.id()))
                .update()
        ;
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
        log.debug("Update for {} failed. Cause: {}", repository.identifier(), reason);

        query(update)
                .single(call().bind(reason).bind(repository.id()))
                .update();
    }

    public record RepositoryData(Instant updated, Instant checked, String commit, String branch) {
    }
}
