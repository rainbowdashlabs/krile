/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.core;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.chojo.krile.configuration.Configuration;
import de.chojo.krile.configuration.elements.BaseSettings;
import de.chojo.krile.data.access.TagData;
import de.chojo.krile.service.TagInteractionListener;
import dev.goldmensch.fluava.Bundle;
import dev.goldmensch.fluava.Fluava;
import io.github.kaktushose.jdac.JDACBuilder;
import io.github.kaktushose.jdac.JDACommands;
import io.github.kaktushose.jdac.annotations.interactions.CommandScope;
import io.github.kaktushose.jdac.definitions.interactions.command.CommandDefinition;
import io.github.kaktushose.jdac.embeds.error.DefaultErrorMessageFactory;
import io.github.kaktushose.jdac.guice.GuiceExtensionData;
import io.github.kaktushose.jdac.message.i18n.FluavaLocalizer;
import io.github.kaktushose.jdac.property.JDACProperty;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.Set;

/**
 * Connects Krile to Discord and registers its interactions with JDA-Commands.
 * <p>
 * Commands are registered globally and offered in guilds. Commands that also work in direct messages set this in
 * their own {@code @CommandConfig}. With {@link BaseSettings#testMode()} commands are registered in the bot guild
 * only, except those with their own {@code @CommandConfig}, which replaces the global one.
 */
@Singleton
public class Bot {
    private final Threading threading;
    private final Configuration configuration;
    private final TagData tags;
    private final Fluava fluava;
    private final Bundle texts;
    private final Injector injector;
    private ShardManager shardManager;

    @Inject
    public Bot(
            Threading threading,
            Configuration configuration,
            TagData tags,
            Fluava fluava,
            Bundle texts,
            Injector injector) {
        this.threading = threading;
        this.configuration = configuration;
        this.tags = tags;
        this.fluava = fluava;
        this.texts = texts;
        this.injector = injector;
    }

    public ShardManager shardManager() {
        return shardManager;
    }

    /**
     * Connects to Discord, waits until every shard is ready and registers the interactions.
     *
     * @throws InterruptedException when interrupted while waiting for the shards
     */
    public void start() throws InterruptedException {
        initShardManager();
        for (JDA shard : shardManager.getShards()) {
            shard.awaitReady();
        }
        initInteractions();
    }

    private void initShardManager() {
        shardManager = DefaultShardManagerBuilder.createDefault(
                        configuration.main().baseSettings().token())
                .enableIntents(GatewayIntent.DIRECT_MESSAGES)
                .setEnableShutdownHook(false)
                .setThreadFactory(Threading.createThreadFactory(threading.jdaGroup()))
                .setEventPool(threading.jdaWorker())
                .addEventListeners(new TagInteractionListener(texts, tags))
                .build();
    }

    private void initInteractions() {
        BaseSettings settings = configuration.main().baseSettings();
        JDACBuilder builder = JDACommands.builder(shardManager)
                .packages("de.chojo.krile")
                .extensionData(new GuiceExtensionData(injector))
                .localizer(FluavaLocalizer.create(fluava))
                .globalCommandConfig(CommandDefinition.CommandConfig.of(config -> {
                    config.context(InteractionContextType.GUILD);
                    if (settings.testMode()) config.scope(CommandScope.GUILD);
                }))
                .setProperty(
                        JDACProperty.ERROR_MESSAGE_FACTORY,
                        introspection -> new LoggingErrorMessageFactory(
                                new DefaultErrorMessageFactory(introspection.get(JDACProperty.MESSAGE_RESOLVER))));
        if (settings.testMode()) {
            builder.guildScopeProvider(command -> Set.of(settings.botGuild()));
        }
        builder.start();
    }
}
