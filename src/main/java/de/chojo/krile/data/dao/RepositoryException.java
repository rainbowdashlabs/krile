/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao;

import de.chojo.krile.tagimport.repo.RawRepository;

/**
 * Represents an exception that occurs when a repository update fails.
 * Extends the RuntimeException class.
 */

public class RepositoryException extends RuntimeException {
    /**
     * Creates a new instance of RepositoryException with the given repository and message.
     *
     * @param repo    the RawRepository object representing the repository that failed to update
     * @param message the error message associated with the exception
     */
    public RepositoryException(RawRepository repo, String message) {
        super("Failed to update Repo: %s%n%s".formatted(repo.url(), message));
    }

    /**
     * Constructs a new RepositoryException with the specified repository, message, and cause.
     *
     * @param repo    the RawRepository associated with the exception
     * @param message the detailed error message
     * @param cause   the cause of the exception
     */
    public RepositoryException(RawRepository repo, String message, Throwable cause) {
        super("Failed to update Repo: %s%n%s".formatted(repo.url(), message), cause);
    }

    /**
     * Constructs a new RepositoryException object with the specified repository and cause.
     *
     * @param repo  the repository that failed to update
     * @param cause the cause of the exception
     */
    public RepositoryException(RawRepository repo, Throwable cause) {
        super("Failed to update Repo: %s".formatted(repo.url()), cause);
    }


}
