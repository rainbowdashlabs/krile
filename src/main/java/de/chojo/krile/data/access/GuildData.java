package de.chojo.krile.data.access;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.data.dao.TagGuild;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;

public class GuildData {
    private final Configuration<ConfigFile> configuration;
    private final AuthorData authors;
    private final CategoryData categories;

    public GuildData(Configuration<ConfigFile>configuration, AuthorData authors, CategoryData categories) {
        this.configuration = configuration;
        this.authors = authors;
        this.categories = categories;
    }

    public TagGuild guild(Guild guild) {
        return new TagGuild(guild, configuration, categories, authors);
    }
    public TagGuild guild(GenericInteractionCreateEvent event) {
        return new TagGuild(event.getGuild(), configuration, categories, authors);
    }
}
