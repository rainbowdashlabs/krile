/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.data.access.AuthorData;
import de.chojo.krile.data.access.CategoryData;
import de.chojo.krile.data.dao.tagguild.Repositories;
import de.chojo.krile.data.dao.tagguild.Tags;
import net.dv8tion.jda.api.entities.Guild;

public class TagGuild {
    private final Guild guild;
    private final Repositories repositories;
    private final Tags tags;

    public TagGuild(Guild guild, Configuration<ConfigFile> configuration, CategoryData categories, AuthorData authors) {
        this.guild = guild;
        repositories = new Repositories(this, configuration, authors, categories);
        tags = new Tags(this, repositories, categories, authors);
    }

    public Tags tags() {
        return tags;
    }

    public Repositories repositories() {
        return repositories;
    }

    public Guild guild() {
        return guild;
    }

    public long id() {
        return guild.getIdLong();
    }
}
