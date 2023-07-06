package de.chojo.krile.commands.info;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.krile.commands.info.handler.Default;
import de.chojo.krile.configuration.ConfigFile;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.slf4j.LoggerFactory.getLogger;

public class Info implements SlashProvider<Slash> {
    private static final Logger log = getLogger(Info.class);
    private final String version;
    private final Configuration<ConfigFile> configuration;

    private Info(String version, Configuration<ConfigFile> configuration) {
        this.version = version;
        this.configuration = configuration;
    }

    public static Info create(Configuration<ConfigFile> configuration) {
        var version = "undefined";
        try (var input = Info.class.getClassLoader().getResourceAsStream("version")) {
            version = new String(input.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            log.error("Could not determine version.");
        }
        return new Info(version, configuration);
    }

    @Override
    public Slash slash() {
        return Slash.of("info", "Bot information")
                .unlocalized()
                .command(new Default(version, configuration))
                .build();
    }

}
