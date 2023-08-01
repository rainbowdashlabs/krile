/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile;

import de.chojo.krile.tagimport.exception.ImportException;
import de.chojo.krile.tagimport.exception.ParsingException;
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
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

class RawRepositoryTest {

    private static RawRepository root;
    private static RawRepository sub;

    @BeforeAll
    static void beforeAll() throws GitAPIException, IOException {
        root = TestRepository.root();
        sub = TestRepository.sub();
    }

    public static List<Arguments> repos() {
        return List.of(Arguments.of(root), Arguments.of(sub));
    }

    @ParameterizedTest
    @MethodSource("repos")
    void configuration(RawRepository repo) throws ParsingException {
        RepoConfig configuration = repo.configuration();
        Assertions.assertEquals("tags", configuration.directory());
        Assertions.assertEquals(List.of("ignored_tag"), configuration.exclude());
        Assertions.assertEquals(Collections.emptyList(), configuration.include());
    }

    @Test
    void tags() throws ImportException, ParsingException {
        List<RawTag> tags = sub.tags();
        Assertions.assertEquals(2, tags.size());
    }
    @Test
    void tagsDeep() throws ImportException, ParsingException, NoSuchFieldException, IllegalAccessException {
        // Change tags to deep
        Field configuration = RawRepository.class.getDeclaredField("configuration");
        RepoConfig oldConf = sub.configuration();
        RepoConfig newConf = RepoConfig.create(oldConf.name(), oldConf.description(), oldConf.category(), oldConf.publicRepo(), oldConf.language(), oldConf.directory(), oldConf.include(), oldConf.exclude(), true);
        configuration.setAccessible(true);
        configuration.set(sub, newConf);
        List<RawTag> tags = sub.tags();
        Assertions.assertEquals(3, tags.size());
        Assertions.assertTrue(tags.stream().anyMatch(t -> t.meta().id().equals("sub/sub_tag")));
        Assertions.assertTrue(tags.stream().anyMatch(t -> t.meta().id().equals("default_tag")));
        Assertions.assertTrue(tags.stream().anyMatch(t -> t.meta().id().equals("test tag")));
        configuration.set(sub, oldConf);

    }

    @AfterAll
    static void afterAll() throws IOException {
        //repo.close();
    }
}
