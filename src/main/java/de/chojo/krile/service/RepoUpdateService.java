/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.service;

import de.chojo.jdautil.configuration.Configuration;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.core.Threading;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.tagimport.exception.ImportException;
import de.chojo.krile.tagimport.exception.ParsingException;
import de.chojo.krile.tagimport.repo.RawRepository;
import de.chojo.logutil.marker.LogNotify;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * The RepoUpdateService class represents a service for scheduling and performing updates for repositories.
 * It implements the Runnable interface, allowing it to be scheduled and executed by a separate thread.
 */
public class RepoUpdateService implements Runnable {
    private static final Logger log = getLogger(RepoUpdateService.class);
    private final Configuration<ConfigFile> configuration;
    private final RepositoryData repositoryData;
    private final Queue<String> priorityQueue = new ArrayDeque<>();
    private final Queue<String> repositoryQueue = new ArrayDeque<>();

    RepoUpdateService(Configuration<ConfigFile> configuration, RepositoryData repositoryData) {
        this.configuration = configuration;
        this.repositoryData = repositoryData;
    }

    /**
     * Creates a new instance of RepoUpdateService.
     *
     * @param threading the Threading object to be used for scheduling repository updates
     * @param configuration the Configuration object containing the configuration details
     * @param repositoryData the RepositoryData object containing the repository data
     * @return a new instance of RepoUpdateService
     */
    public static RepoUpdateService create(Threading threading, Configuration<ConfigFile> configuration, RepositoryData repositoryData) {
        log.info("Starting repository update service");
        RepoUpdateService repoUpdateService = new RepoUpdateService(configuration, repositoryData);
        threading.botWorker().scheduleAtFixedRate(repoUpdateService, 30, 30, TimeUnit.SECONDS);
        return repoUpdateService;
    }

    /**
     * Schedules a repository for update.
     *
     * @param repository the repository to be scheduled for update
     */
    public void schedule(Repository repository) {
        if (priorityQueue.contains(repository.url())) return;
        repositoryQueue.remove(repository.url());
        priorityQueue.add(repository.url());
    }

    @Override
    public void run() {
        if (repositoryQueue.isEmpty()) {
            schedule();
        }
        if (!priorityQueue.isEmpty()) {
            update(priorityQueue.poll());
            return;
        }
        if (!repositoryQueue.isEmpty()) {
            update(repositoryQueue.poll());
        }
    }

    /**
     * Updates the specified repository and its associated repositories.
     *
     * @param repository the URL of the repository to be updated
     */
    private void update(String repository) {
        List<Repository> repositories = repositoryData.byUrl(repository);

        Repository first = repositories.get(0);
        log.info("Checking {} for updates", repository);
        try (var flat = RawRepository.remote(configuration, first.identifier(), true)) {
            if (flat.currentCommit().equals(first.data().get().commit())) {
                log.info("Repository {} is up to date", repository);
                for (Repository repo : repositories) repo.checked();
                return;
            }
        } catch (ImportException | IOException e) {
            log.error(LogNotify.NOTIFY_ADMIN, "Could not check repository {} for updates", repository, e);
        } catch (ParsingException e) {
            for (Repository repo : repositories) repo.updateFailed(e.getMessage());
            return;
        }

        log.info("Repository {} is outdated. Performing update for {} outdated repositories", first.url(), repositories.size());
        try (var raw = RawRepository.remote(configuration, first.identifier())) {
            for (Repository repo : repositories) {
                updateRepository(raw.updateIdentifier(repo.identifier()), repo);
            }
        } catch (IOException e) {
            log.error(LogNotify.NOTIFY_ADMIN, "Could not update repository {}", repository, e);
            return;
        } catch (CancellationException e) {
            log.error(LogNotify.NOTIFY_ADMIN, "Repository {} timed out during update", repository);
        } catch (Throwable e) {
            log.error(LogNotify.NOTIFY_ADMIN, "Severe error during repository update of {}", repository, e);
        }
        log.info("Updated {}.", repository);
    }

    /**
     * Schedules the update of repositories.
     * The method adds the least updated repositories to the repository queue for further processing.
     */
    void schedule() {
        repositoryQueue.addAll(repositoryData.leastUpdated(configuration.config().repositories().check(), 10));
    }

    /**
     * Updates a repository.
     * The method runs the update process for the given repository asynchronously.
     * If the update takes longer than 1 minute, it is considered failed.
     *
     * @param raw The raw repository data used for update.
     * @param repo The repository to be updated.
     */
    private void updateRepository(RawRepository raw, Repository repo) {
        var update = CompletableFuture.runAsync(() -> {
            try {
                log.info(LogNotify.STATUS, "Updating {}", repo);
                repo.update(raw);
            } catch (ImportException e) {
                log.error(LogNotify.NOTIFY_ADMIN, "Could not update repository {}", repo, e);
            } catch (ParsingException e) {
                repo.updateFailed(e.getMessage());
            }
        });
        try {
            update.orTimeout(1, TimeUnit.MINUTES).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof TimeoutException) repo.updateFailed("Update took too long.");
            else repo.updateFailed(e.getCause().getMessage());
        }
    }
}
