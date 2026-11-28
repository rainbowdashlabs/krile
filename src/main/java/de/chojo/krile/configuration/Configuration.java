/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.configuration;

import dev.chojo.ocular.Configurations;
import dev.chojo.ocular.dataformats.JsonDataFormat;
import dev.chojo.ocular.dataformats.YamlDataFormat;
import dev.chojo.ocular.key.Key;
import org.slf4j.Logger;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.MapperBuilder;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * The configuration of Krile, stored as YAML.
 * <p>
 * The base directory comes from {@code BOT_CONFIG_BASE} or {@code -Dbot.config.base} and defaults to {@code config}.
 * The file inside it comes from {@code BOT_CONFIG} or {@code -Dbot.config} and defaults to {@code config.yml}.
 * <p>
 * Earlier versions stored the configuration as {@code config.json} in the same directory. When the YAML file does not
 * exist yet and that JSON file does, it is converted once; the JSON file is left untouched. The JSON file stays loaded
 * afterwards, so saving every loaded file at once would write it again; save the main key only.
 * <p>
 * Earlier versions also took the whole path of the file from {@code BOT_CONFIG}, typically {@code config/config.json}.
 * Such a value is refused, because it now names a file inside the base directory.
 * <p>
 * Unknown keys are ignored, so a file written for another version still loads.
 */
public class Configuration extends Configurations<ConfigFile> {
    private static final Logger log = getLogger(Configuration.class);
    private static final Path LEGACY_FILE = Path.of("config.json");

    private Configuration(Path base, Key<ConfigFile> main) {
        super(
                base,
                main,
                List.of(new YamlDataFormat(), new JsonDataFormat(true)),
                Configuration.class.getClassLoader(),
                null);
    }

    /**
     * Creates the configuration from the paths set in the environment, converting a legacy JSON file first.
     *
     * @return the configuration
     */
    public static Configuration create() {
        return create(basePath(), rootPath());
    }

    /**
     * Creates the configuration for a file in a base directory, converting a legacy JSON file first.
     *
     * @param base directory relative file paths are resolved against
     * @param file configuration file
     * @return the configuration
     */
    public static Configuration create(Path base, Path file) {
        if (file.getFileName().toString().endsWith(".json")) {
            throw new IllegalStateException(("The configuration file is now YAML inside %s. Remove the old setting "
                            + "BOT_CONFIG or -Dbot.config=%s and keep that file as %s; it is converted to "
                            + "config.yml on start.")
                    .formatted(base, file, base.resolve(LEGACY_FILE)));
        }
        Key<ConfigFile> main = key(file);
        Configuration configuration = new Configuration(base, main);
        configuration.convertLegacyFile(main);
        return configuration;
    }

    @Override
    public void configureReader(MapperBuilder<ObjectMapper, ?> builder) {
        builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    private void convertLegacyFile(Key<ConfigFile> main) {
        Key<ConfigFile> legacy = key(LEGACY_FILE);
        if (exists(main) || !exists(legacy)) return;
        log.info("Converting {} to {}", LEGACY_FILE, main.path());
        migrate(legacy, main);
    }

    private static Key<ConfigFile> key(Path file) {
        return Key.builder(file, ConfigFile::new).strict().build();
    }

    private static Path rootPath() {
        return Optional.ofNullable(System.getProperty("bot.config"))
                .or(() -> Optional.ofNullable(System.getenv("BOT_CONFIG")))
                .map(Path::of)
                .orElse(Path.of("config.yml"));
    }

    private static Path basePath() {
        return Optional.ofNullable(System.getProperty("bot.config.base"))
                .or(() -> Optional.ofNullable(System.getenv("BOT_CONFIG_BASE")))
                .map(Path::of)
                .orElse(Path.of("config"));
    }
}
