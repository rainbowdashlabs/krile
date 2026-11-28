/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.chojo.jdautil.interactions.dispatching.InteractionHub;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.krile.commands.discover.Discover;
import de.chojo.krile.commands.info.Info;
import de.chojo.krile.commands.preview.Preview;
import de.chojo.krile.commands.repositories.Repositories;
import de.chojo.krile.commands.repository.Repository;
import de.chojo.krile.commands.tag.Tag;
import de.chojo.krile.commands.tags.Tags;
import de.chojo.krile.configuration.Configuration;
import de.chojo.krile.data.access.CategoryData;
import de.chojo.krile.data.access.GuildData;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.data.access.TagData;
import de.chojo.krile.service.RepoUpdateService;
import de.chojo.krile.service.TagInteractionListener;
import de.chojo.logutil.marker.LogNotify;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class Bot {
    private static final Logger log = getLogger(Bot.class);
    private final Threading threading;
    private final Configuration configuration;
    private final GuildData guilds;
    private final RepositoryData repositories;
    private final TagData tags;
    private final CategoryData categories;
    private final RepoUpdateService repoUpdateService;
    private ShardManager shardManager;
    private Localizer localizer;

    @Inject
    public Bot(
            Threading threading,
            Configuration configuration,
            GuildData guilds,
            RepositoryData repositories,
            TagData tags,
            CategoryData categories,
            RepoUpdateService repoUpdateService) {
        this.threading = threading;
        this.configuration = configuration;
        this.guilds = guilds;
        this.repositories = repositories;
        this.tags = tags;
        this.categories = categories;
        this.repoUpdateService = repoUpdateService;
    }

    public ShardManager shardManager() {
        return shardManager;
    }

    /**
     * Connects to Discord and registers the interactions.
     */
    public void start() {
        initServices();
        initShardManager();
        initInteractions();
    }

    private void initServices() {
        localizer = Localizer.builder(DiscordLocale.ENGLISH_US)
                .addLanguage(DiscordLocale.GERMAN)
                // TODO: replace with database access
                // Or just use guild locale
                .withLanguageProvider(guild -> Optional.empty())
                .withGuildLocaleCodeProvider((guild, key) -> Optional.empty())
                .build();
    }

    private void initShardManager() {
        shardManager = DefaultShardManagerBuilder.createDefault(
                        configuration.main().baseSettings().token())
                .enableIntents(GatewayIntent.DIRECT_MESSAGES)
                .setEnableShutdownHook(false)
                .setThreadFactory(Threading.createThreadFactory(threading.jdaGroup()))
                .setEventPool(threading.jdaWorker())
                .addEventListeners(new TagInteractionListener(localizer, tags))
                .build();
    }

    private void initInteractions() {
        InteractionHub.builder(shardManager)
                .withCommandErrorHandler((context, throwable) -> {
                    log.error(
                            LogNotify.NOTIFY_ADMIN,
                            "Command execution of {} failed\n{}",
                            context.interaction().meta().name(),
                            context.args(),
                            throwable);
                })
                .withGuildCommandMapper(cmd -> Collections.singletonList(
                        configuration.main().baseSettings().botGuild()))
                .withDefaultMenuService()
                .withPagination(builder -> builder.previousText("").nextText(""))
                .withDefaultModalService()
                .withLocalizer(localizer)
                .withCommands(
                        new Repository(repositories, guilds, configuration, repoUpdateService),
                        new Tag(guilds),
                        Info.create(configuration),
                        new Tags(guilds),
                        new Repositories(guilds),
                        new Discover(repositories, tags, configuration, categories),
                        new Preview())
                .build();
    }
}
