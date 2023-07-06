/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao;

import de.chojo.krile.tagimport.repo.RawRepository;

public class RepositoryUpdateException extends RuntimeException {
    public RepositoryUpdateException(RawRepository repo, String message) {
        super("Failed to update Repo: %s%n%s".formatted(repo.url(), message));
    }
    public RepositoryUpdateException(RawRepository repo, String message, Throwable cause) {
        super("Failed to update Repo: %s%n%s".formatted(repo.url(), message), cause);
    }
    public RepositoryUpdateException(RawRepository repo, Throwable cause) {
        super("Failed to update Repo: %s".formatted(repo.url()), cause);
    }


}
