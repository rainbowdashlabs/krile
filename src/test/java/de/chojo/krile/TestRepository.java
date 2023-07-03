package de.chojo.krile;

import de.chojo.krile.configuration.elements.RepositoryLocation;
import de.chojo.krile.tagimport.repo.RawTagRepository;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Path;

public class TestRepository {
    public static RawTagRepository standard () throws GitAPIException, IOException {
        return RawTagRepository.create(Path.of(".git", "modules", "krile-tags"),Path.of("krile-tags"), new RepositoryLocation("GitHub", "https://github.com/%s.git", "https://github.com"), "rainbowdashlabs", "krile-tags");
    }
}
