package de.chojo.krile;

import de.chojo.krile.configuration.elements.RepositoryLocation;
import de.chojo.krile.tagimport.repo.RawTagRepository;
import de.chojo.krile.tagimport.repo.RepoConfig;
import de.chojo.krile.tagimport.tag.RawTag;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

class TagRepositoryTest {

    private static RawTagRepository repo;

    @BeforeAll
    static void beforeAll() throws GitAPIException, IOException {
        repo = TestRepository.standard();
    }

    @Test
    void configuration() throws IOException {
        RepoConfig configuration = repo.configuration();
        Assertions.assertEquals("tags", configuration.directory());
        Assertions.assertEquals(List.of("ignored_tag"), configuration.exclude());
        Assertions.assertEquals(Collections.emptyList(), configuration.include());
    }

    @Test
    void tags() throws IOException {
        List<RawTag> tags = repo.tags();
        Assertions.assertEquals(2, tags.size());
    }

    @AfterAll
    static void afterAll() throws IOException {
        //repo.close();
    }
}
