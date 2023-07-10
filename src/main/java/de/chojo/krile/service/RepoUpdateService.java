/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.service;

import de.chojo.jdautil.configuratino.Configuration;
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
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class RepoUpdateService implements Runnable {
    private static final Logger log = getLogger(RepoUpdateService.class);
    private final Configuration<ConfigFile> configuration;
    private final RepositoryData repositoryData;
    private final Queue<Repository> priorityQueue = new ArrayDeque<>();
    private final Queue<Repository> repositoryQueue = new ArrayDeque<>();

    RepoUpdateService(Configuration<ConfigFile> configuration, RepositoryData repositoryData) {
        this.configuration = configuration;
        this.repositoryData = repositoryData;
    }

    public static RepoUpdateService create(Threading threading, Configuration<ConfigFile> configuration, RepositoryData repositoryData) {
        log.info("Starting repository update service");
        RepoUpdateService repoUpdateService = new RepoUpdateService(configuration, repositoryData);
        threading.botWorker().scheduleAtFixedRate(repoUpdateService, 30, 30, TimeUnit.SECONDS);
        return repoUpdateService;
    }

    public void schedule(Repository repository) {
        if (priorityQueue.contains(repository)) return;
        repositoryQueue.remove(repository);
        priorityQueue.add(repository);
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

    public void update(Repository repository) {
        log.info("Checking {} for updates", repository);
        try (var flat = RawRepository.remote(configuration, repository.identifier(), true)) {
            if (flat.currentCommit().equals(repository.data().get().commit())) {
                log.info("Repository {} is up to date", repository);
                repository.checked();
                return;
            }
        } catch (ImportException | IOException e) {
            log.error(LogNotify.NOTIFY_ADMIN, "Could not check repository {} for updates", repository, e);
        } catch (ParsingException e) {
            repository.updateFailed(e.getMessage());
        }
        log.info("Repository {} is outdated. Performing update", repository);
        try (var raw = RawRepository.remote(configuration, repository.identifier())) {
            repository.update(raw);
        } catch (ImportException | IOException e) {
            log.error(LogNotify.NOTIFY_ADMIN, "Could not update repository {}", repository, e);
            return;
        } catch (ParsingException e) {
            repository.updateFailed(e.getMessage());
        }catch (Throwable e){
            log.error(LogNotify.NOTIFY_ADMIN, "Severe error during repository update of {}", repository, e);
        }
        log.info("Updated {}.", repository);
    }

    void schedule() {
        repositoryQueue.addAll(repositoryData.leastUpdated(configuration.config().repositories().check(), 10));
    }
}
