package de.chojo.krile.commands.discover.handler.tags;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.pagination.bag.ListPageBag;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.data.access.TagData;
import de.chojo.krile.data.dao.repository.tags.Tag;
import de.chojo.krile.data.util.TagFilter;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SearchTag implements SlashHandler {
    private final TagData tagData;

    public SearchTag(TagData tagData) {
        this.tagData = tagData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Integer category = event.getOption("category", null, OptionMapping::getAsInt);
        String language = event.getOption("language", null, OptionMapping::getAsString);
        String name = event.getOption("name", null, OptionMapping::getAsString);

        TagFilter tagFilter = new TagFilter(category, language, name);
        List<Tag> search = tagData.search(tagFilter);
        var page = new ListPageBag<>(search) {
            @Override
            public CompletableFuture<MessageEditData> buildPage() {
                return CompletableFuture.completedFuture(MessageEditData.fromEmbeds(currentElement().infoEmbed(context)));
            }
        };
        context.registerPage(page, true);

    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        SlashHandler.super.onAutoComplete(event, context);
    }
}
