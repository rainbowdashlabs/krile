/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import de.chojo.jdautil.configuration.Configuration;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.data.access.AuthorData;
import de.chojo.krile.data.access.CategoryData;
import de.chojo.krile.data.access.GuildData;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.data.access.TagData;
import de.chojo.krile.service.RepoUpdateService;
import net.dv8tion.jda.api.sharding.ShardManager;

/**
 * Binds the configuration, the threading setup, the data access classes and the services of Krile.
 * <p>
 * The database is set up before the injector exists, so the data access classes are bound as the instances
 * {@link Data} created.
 */
public class KrileModule extends AbstractModule {
    private final Configuration<ConfigFile> configuration;
    private final Threading threading;
    private final Data data;

    public KrileModule(Configuration<ConfigFile> configuration, Threading threading, Data data) {
        this.configuration = configuration;
        this.threading = threading;
        this.data = data;
    }

    @Override
    protected void configure() {
        bind(new TypeLiteral<Configuration<ConfigFile>>() {}).toInstance(configuration);
        bind(Threading.class).toInstance(threading);
        bind(Data.class).toInstance(data);
        bind(AuthorData.class).toInstance(data.authors());
        bind(CategoryData.class).toInstance(data.categories());
        bind(GuildData.class).toInstance(data.guilds());
        bind(RepositoryData.class).toInstance(data.repositories());
        bind(TagData.class).toInstance(data.tags());
    }

    @Provides
    @Singleton
    public RepoUpdateService repoUpdateService(RepositoryData repositoryData) {
        return RepoUpdateService.create(threading, configuration, repositoryData);
    }

    /**
     * Provides the shard manager of the started bot.
     *
     * @param bot the bot, started by {@link de.chojo.krile.Krile} before anything that needs the shard manager
     * @return the shard manager
     */
    @Provides
    public ShardManager shardManager(Bot bot) {
        return bot.shardManager();
    }
}
