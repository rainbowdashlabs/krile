package de.chojo.krile.data.dao;

import de.chojo.krile.data.access.Authors;
import de.chojo.krile.data.access.Categories;
import de.chojo.krile.data.dao.tagguild.Repositories;
import de.chojo.krile.data.dao.tagguild.Tags;
import net.dv8tion.jda.api.entities.Guild;

public class TagGuild {
    private final Guild guild;
    private final Repositories repositories;
    private final Tags tags;

    public Tags tags() {
        return tags;
    }

    public Repositories repositories() {
        return repositories;
    }

    public TagGuild(Guild guild, Categories categories, Authors authors) {
        this.guild = guild;
        repositories = new Repositories(this, authors, categories);
        tags = new Tags(this, categories, authors);
    }



    public Guild guild() {
        return guild;
    }

    public long id() {
        return guild.getIdLong();
    }
}
