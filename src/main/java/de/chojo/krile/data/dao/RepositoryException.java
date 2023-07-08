/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao;

import de.chojo.krile.tagimport.repo.RawRepository;

public class RepositoryException extends RuntimeException {
    public RepositoryException(RawRepository repo, String message) {
        super("Failed to update Repo: %s%n%s".formatted(repo.url(), message));
    }

    public RepositoryException(RawRepository repo, String message, Throwable cause) {
        super("Failed to update Repo: %s%n%s".formatted(repo.url(), message), cause);
    }

    public RepositoryException(RawRepository repo, Throwable cause) {
        super("Failed to update Repo: %s".formatted(repo.url()), cause);
    }


}
