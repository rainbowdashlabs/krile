/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import de.chojo.krile.configuration.Configuration;
import de.chojo.krile.data.access.AuthorData;
import de.chojo.krile.data.access.CategoryData;
import de.chojo.krile.data.access.GuildData;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.data.access.TagData;
import de.chojo.krile.service.RepoUpdateService;
import dev.goldmensch.fluava.Bundle;
import dev.goldmensch.fluava.Fluava;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.Locale;

/**
 * Binds the configuration, the threading setup, the data access classes and the services of Krile.
 * <p>
 * The database is set up before the injector exists, so the data access classes are bound as the instances
 * {@link Data} created.
 */
public class KrileModule extends AbstractModule {
    private final Configuration configuration;
    private final Threading threading;
    private final Data data;

    public KrileModule(Configuration configuration, Threading threading, Data data) {
        this.configuration = configuration;
        this.threading = threading;
        this.data = data;
    }

    @Override
    protected void configure() {
        bind(Configuration.class).toInstance(configuration);
        bind(Threading.class).toInstance(threading);
        bind(Data.class).toInstance(data);
        bind(AuthorData.class).toInstance(data.authors());
        bind(CategoryData.class).toInstance(data.categories());
        bind(GuildData.class).toInstance(data.guilds());
        bind(RepositoryData.class).toInstance(data.repositories());
        bind(TagData.class).toInstance(data.tags());
    }

    /**
     * Provides the repository update service. The injector is created in the production stage, so the service and
     * its schedule start together with the injector.
     *
     * @param repositoryData repository access
     * @return the update service
     */
    @Provides
    @Singleton
    public RepoUpdateService repoUpdateService(RepositoryData repositoryData) {
        return RepoUpdateService.create(threading, configuration, repositoryData);
    }

    /**
     * Provides the Fluent texts in {@code locale/<bundle>/<language>.ftl}, falling back to English.
     *
     * @return the Fluava instance
     */
    @Provides
    @Singleton
    public Fluava fluava() {
        return Fluava.builder().bundleRoot("locale").fallback(Locale.ENGLISH).build();
    }

    /**
     * Provides the default bundle, for texts needed outside JDA-Commands events.
     *
     * @param fluava the Fluava instance
     * @return the default bundle
     */
    @Provides
    @Singleton
    public Bundle texts(Fluava fluava) {
        return fluava.loadBundle("default");
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
