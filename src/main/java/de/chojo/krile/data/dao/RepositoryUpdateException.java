package de.chojo.krile.data.dao;

import de.chojo.krile.tagimport.repo.RawTagRepository;

public class RepositoryUpdateException extends RuntimeException {
    public RepositoryUpdateException(RawTagRepository repo, String message) {
        super("Failed to update Repo: %s%n%s".formatted(repo.url(), message));
    }
    public RepositoryUpdateException(RawTagRepository repo, String message, Throwable cause) {
        super("Failed to update Repo: %s%n%s".formatted(repo.url(), message), cause);
    }
    public RepositoryUpdateException(RawTagRepository repo, Throwable cause) {
        super("Failed to update Repo: %s".formatted(repo.url()), cause);
    }


}
