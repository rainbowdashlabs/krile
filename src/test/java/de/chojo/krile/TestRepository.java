/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile;

import de.chojo.krile.configuration.elements.Repositories;
import de.chojo.krile.configuration.elements.RepositoryLocation;
import de.chojo.krile.data.dao.Identifier;
import de.chojo.krile.tagimport.repo.RawRepository;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class TestRepository {
    private static final Path root = new File(".").toPath().toAbsolutePath().getParent();
    private static final RepositoryLocation location = Repositories.GITHUB;
    private static final String user = "rainbowdashlabs";
    private static final String repo = "krile";
    private static final String path = "/sub-tags";

    public static RawRepository root() throws GitAPIException, IOException {
        return RawRepository.local(root, location, Identifier.of(location.name(), user, repo));
    }

    public static RawRepository sub() throws GitAPIException, IOException {
        return RawRepository.local(root, location, Identifier.of(location.name(), user, repo, path));
    }
}
