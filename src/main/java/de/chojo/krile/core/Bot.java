package de.chojo.krile.core;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.jdautil.interactions.dispatching.InteractionHub;
import de.chojo.krile.commands.discover.Discover;
import de.chojo.krile.commands.info.Info;
import de.chojo.krile.commands.repositories.Repositories;
import de.chojo.krile.commands.repository.Repository;
import de.chojo.krile.commands.tag.Tag;
import de.chojo.krile.commands.tags.Tags;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.service.RepoUpdateService;
import de.chojo.logutil.marker.LogNotify;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import java.util.Collections;

import static org.slf4j.LoggerFactory.getLogger;

public class Bot {
    private static final Logger log = getLogger(Bot.class);
    private final Data data;
    private final Threading threading;
    private final Configuration<ConfigFile> configuration;
    private ShardManager shardManager;
        private RepoUpdateService repoUpdateService;

    private Bot(Data data, Threading threading, Configuration<ConfigFile> configuration) {
        this.data = data;
        this.threading = threading;
        this.configuration = configuration;
    }

    public static Bot create(Data data, Threading threading, Configuration<ConfigFile> configuration) {
        Bot bot = new Bot(data, threading, configuration);
        bot.init();
        return bot;
    }

    private void init() {
        initShardManager();
        initServices();
        initInteractions();
    }

    private void initServices() {
        repoUpdateService = RepoUpdateService.create(threading, configuration, data.repositories());
    }

    private void initShardManager() {
        shardManager = DefaultShardManagerBuilder
                .createDefault(configuration.config().baseSettings().token())
                .enableIntents(GatewayIntent.DIRECT_MESSAGES)
                .setEnableShutdownHook(false)
                .setThreadFactory(Threading.createThreadFactory(threading.jdaGroup()))
                .setEventPool(threading.jdaWorker())
                .build();
    }

    private void initInteractions() {
        InteractionHub.builder(shardManager)
                .withCommandErrorHandler((context, throwable) -> {
                    log.error(LogNotify.NOTIFY_ADMIN, "Command execution of {} failed\n{}",
                            context.interaction().meta().name(), context.args(), throwable);
                })
                .withGuildCommandMapper(cmd -> Collections.singletonList(configuration.config().baseSettings()
                        .botGuild()))
                .withDefaultMenuService()
                .withPagination(builder -> builder.previousText("Previous").nextText("Next"))
                .withDefaultModalService()
                .withCommands(
                        new Repository(data.repositories(), data.guilds(), configuration, repoUpdateService),
                        new Tag(data.guilds()),
                        Info.create(configuration),
                        new Tags(data.guilds()),
                        new Repositories(data.guilds()),
                        new Discover())
                .build();
    }

    public ShardManager shardManager() {
        return shardManager;
    }
}
