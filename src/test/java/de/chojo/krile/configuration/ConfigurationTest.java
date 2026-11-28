/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.configuration;

import dev.chojo.ocular.exceptions.ConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationTest {
    private static final String LEGACY_JSON = """
            {
              "baseSettings": {"token": "secret", "botOwner": [1, 2], "botGuild": 42},
              "database": {"host": "db", "poolSize": 9},
              "repositories": {
                "check": 120,
                "repositories": [{
                  "name": "Forgejo",
                  "template": "https://forgejo.example/{user}/{repo}.git",
                  "tld": "https://forgejo.example",
                  "filePath": "https://forgejo.example/{user}/{repo}/src/branch/{branch}/{path}",
                  "dirPath": "https://forgejo.example/{user}/{repo}/src/branch/{branch}/{path}"
                }]
              },
              "removedSetting": true
            }
            """;

    @Test
    void convertsLegacyJson(@TempDir Path base) throws IOException {
        Files.writeString(base.resolve("config.json"), LEGACY_JSON);

        ConfigFile config = Configuration.create(base, Path.of("config.yml")).main();

        assertTrue(Files.exists(base.resolve("config.yml")));
        assertEquals(LEGACY_JSON, Files.readString(base.resolve("config.json")));
        assertEquals("secret", config.baseSettings().token());
        assertEquals(42L, config.baseSettings().botGuild());
        assertTrue(config.baseSettings().isOwner(2));
        assertEquals("db", config.database().host());
        assertEquals(9, config.database().poolSize());
        assertEquals(120, config.repositories().check());
        assertEquals(1, config.repositories().repositories().size());
        assertEquals(
                "https://forgejo.example/me/tags.git",
                config.repositories().repositories().getFirst().url("me", "tags"));
    }

    @Test
    void readsConvertedFileAgain(@TempDir Path base) throws IOException {
        Files.writeString(base.resolve("config.json"), LEGACY_JSON);
        Configuration.create(base, Path.of("config.yml")).main();
        Files.writeString(base.resolve("config.json"), "{\"baseSettings\": {\"token\": \"changed\"}}");

        ConfigFile config = Configuration.create(base, Path.of("config.yml")).main();

        assertEquals("secret", config.baseSettings().token());
    }

    @Test
    void createsDefaultWithoutLegacyFile(@TempDir Path base) {
        ConfigFile config = Configuration.create(base, Path.of("config.yml")).main();

        assertTrue(Files.exists(base.resolve("config.yml")));
        assertFalse(Files.exists(base.resolve("config.json")));
        assertEquals("localhost", config.database().host());
    }

    @Test
    void keepsBrokenLegacyFile(@TempDir Path base) throws IOException {
        Files.writeString(base.resolve("config.json"), "{\"baseSettings\": ");

        assertThrows(ConfigurationException.class, () -> Configuration.create(base, Path.of("config.yml")));
        assertFalse(Files.exists(base.resolve("config.yml")));
        assertEquals("{\"baseSettings\": ", Files.readString(base.resolve("config.json")));
    }

    @Test
    void refusesLegacyFileSetting(@TempDir Path base) {
        assertThrows(IllegalStateException.class, () -> Configuration.create(base, Path.of("config", "config.json")));
    }

    @Test
    void keepsBrokenFile(@TempDir Path base) throws IOException {
        Files.writeString(base.resolve("config.yml"), "baseSettings: [not, an, object");

        Configuration configuration = Configuration.create(base, Path.of("config.yml"));

        assertThrows(ConfigurationException.class, configuration::main);
        assertEquals("baseSettings: [not, an, object", Files.readString(base.resolve("config.yml")));
    }
}
