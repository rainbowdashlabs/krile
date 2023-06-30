package de.chojo.krile;

import de.chojo.krile.tagimport.repo.RepoConfig;
import de.chojo.krile.tagimport.repo.TagRepository;
import de.chojo.krile.tagimport.tag.RawTag;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

class TagRepositoryTest {
    private static final String REPO_URL = "https://github.com/rainbowdashlabs/krile-tags.git";

    private static TagRepository REPO;
    @BeforeAll
    static void beforeAll() throws GitAPIException, IOException {
        REPO = TagRepository.create(REPO_URL);
    }

    @Test
    void configuration() throws IOException {
        RepoConfig configuration = REPO.configuration();
        Assertions.assertEquals("tags", configuration.directory());
        Assertions.assertEquals(List.of("ignored_tag"), configuration.exclude());
        Assertions.assertEquals(Collections.emptyList(), configuration.include());
    }

    @Test
    void tags() throws IOException {
        List<RawTag> tags = REPO.tags();
        Assertions.assertEquals(2, tags.size());
    }

    @AfterAll
    static void afterAll() throws IOException {
        REPO.close();
    }
}
