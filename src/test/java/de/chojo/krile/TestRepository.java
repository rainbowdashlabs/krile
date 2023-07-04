package de.chojo.krile;

import de.chojo.krile.configuration.elements.RepositoryLocation;
import de.chojo.krile.tagimport.repo.RawRepository;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;

public class TestRepository {
    public static RawRepository root() throws GitAPIException, IOException {
        return RawRepository.root(new File(".").toPath().toAbsolutePath().getParent(), new RepositoryLocation("GitHub", "https://github.com/%s.git", "https://github.com"), "rainbowdashlabs", "krile");
    }
    public static RawRepository sub() throws GitAPIException, IOException {
        return RawRepository.sub(new File(".").toPath().toAbsolutePath().getParent(), new RepositoryLocation("GitHub", "https://github.com/%s.git", "https://github.com"),"/test-tags", "rainbowdashlabs", "krile");
    }
}
