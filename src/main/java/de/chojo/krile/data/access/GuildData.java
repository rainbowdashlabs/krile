/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.access;

import de.chojo.jdautil.configuration.Configuration;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.data.dao.TagGuild;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;

public class GuildData {
    private final Configuration<ConfigFile> configuration;
    private final AuthorData authors;
    private final CategoryData categories;

    public GuildData(Configuration<ConfigFile> configuration, AuthorData authors, CategoryData categories) {
        this.configuration = configuration;
        this.authors = authors;
        this.categories = categories;
    }

    /**
     * Creates a TagGuild object.
     *
     * @param guild The Guild object to be associated with the TagGuild.
     * @return A new TagGuild object.
     */
    public TagGuild guild(Guild guild) {
        return new TagGuild(guild, configuration, categories, authors);
    }

    /**
     * Creates a TagGuild instance based on the specified GenericInteractionCreateEvent.
     *
     * @param event The GenericInteractionCreateEvent that triggered the guild creation.
     * @return A new TagGuild instance.
     */
    public TagGuild guild(GenericInteractionCreateEvent event) {
        return new TagGuild(event.getGuild(), configuration, categories, authors);
    }
}
