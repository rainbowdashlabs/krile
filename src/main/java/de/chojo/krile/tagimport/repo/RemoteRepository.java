/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.tagimport.repo;

import de.chojo.krile.data.dao.Identifier;
import org.eclipse.jgit.api.Git;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Represents a remote repository that extends RawRepository and implements Closeable.
 * This class provides a way to interact with remote repositories and perform operations on them.
 */
public class RemoteRepository extends RawRepository implements Closeable {
    protected RemoteRepository(String url, Identifier identifier, Path root, Git git) {
        super(url, identifier, root, git);
    }

    /**
     * Closes the current instance and performs cleanup operations.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        git().close();
        try (var stream = Files.walk(root())) {
            stream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}
