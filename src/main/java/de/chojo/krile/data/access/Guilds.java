package de.chojo.krile.data.access;

import de.chojo.krile.data.dao.TagGuild;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;

public class Guilds {
    private final Authors authors;
    private final Categories categories;

    public Guilds(Authors authors, Categories categories) {
        this.authors = authors;
        this.categories = categories;
    }

    public TagGuild guild(Guild guild) {
        return new TagGuild(guild, categories, authors);
    }
    public TagGuild guild(GenericInteractionCreateEvent event) {
        return new TagGuild(event.getGuild(), categories, authors);
    }
}
