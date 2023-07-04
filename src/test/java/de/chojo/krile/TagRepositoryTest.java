package de.chojo.krile;

import de.chojo.krile.tagimport.repo.RawRepository;
import de.chojo.krile.tagimport.repo.RepoConfig;
import de.chojo.krile.tagimport.tag.RawTag;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

class TagRepositoryTest {

    private static RawRepository root;
    private static RawRepository sub;

    @BeforeAll
    static void beforeAll() throws GitAPIException, IOException {
        root = TestRepository.root();
        sub = TestRepository.sub();
    }

    public static List<Arguments> repos() throws GitAPIException, IOException {
        return List.of(Arguments.of(root), Arguments.of(sub));
    }

    @ParameterizedTest
    @MethodSource("repos")
    void configuration() throws IOException {
        RepoConfig configuration = root.configuration();
        Assertions.assertEquals("tags", configuration.directory());
        Assertions.assertEquals(List.of("ignored_tag"), configuration.exclude());
        Assertions.assertEquals(Collections.emptyList(), configuration.include());
    }

    @Test
    void tags() throws IOException {
        List<RawTag> tags = root.tags();
        Assertions.assertEquals(2, tags.size());
    }

    @AfterAll
    static void afterAll() throws IOException {
        //repo.close();
    }
}
