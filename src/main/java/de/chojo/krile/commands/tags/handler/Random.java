package de.chojo.krile.commands.tags.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.commands.tag.handler.Show;
import de.chojo.krile.data.access.Guilds;
import de.chojo.krile.data.dao.repository.tags.Tag;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Optional;

public class Random implements SlashHandler {
    private final Guilds guilds;

    public Random(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Optional<Tag> random = guilds.guild(event).tags().random();
        if (random.isEmpty()) {
            event.reply("No tags registered on this guild").queue();
            return;
        }
        Show.showTag(event, context, guilds.guild(event), random.get());
    }
}
