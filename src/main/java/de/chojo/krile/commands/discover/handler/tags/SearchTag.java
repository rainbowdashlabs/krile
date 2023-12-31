/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.discover.handler.tags;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.pagination.bag.IPageBag;
import de.chojo.jdautil.pagination.bag.PageBuilder;
import de.chojo.jdautil.util.Choice;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.data.access.CategoryData;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.data.access.TagData;
import de.chojo.krile.data.dao.Category;
import de.chojo.krile.data.dao.repository.tags.Tag;
import de.chojo.krile.data.util.TagFilter;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.List;
import java.util.Optional;

public class SearchTag implements SlashHandler {
    private final TagData tagData;
    private final RepositoryData repositoryData;
    private final CategoryData categoryData;

    public SearchTag(TagData tagData, RepositoryData repositoryData, CategoryData categoryData) {
        this.tagData = tagData;
        this.repositoryData = repositoryData;
        this.categoryData = categoryData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        String category = event.getOption("category", null, OptionMapping::getAsString);
        String language = event.getOption("language", null, OptionMapping::getAsString);
        String name = event.getOption("name", null, OptionMapping::getAsString);

        Optional<Category> resolve = categoryData.resolve(category);

        if (resolve.isEmpty() && category != null) {
            event.reply(context.localize("error.category.unknown")).setEphemeral(true).queue();
            return;
        }

        TagFilter tagFilter = new TagFilter(resolve.map(Category::id).orElse(null), language, name);
        List<Tag> search = tagData.search(tagFilter);
        IPageBag page = PageBuilder.list(search)
                .syncPage(p -> MessageEditData.fromEmbeds(p.currentElement().infoEmbed(context.guildLocalizer())))
                .syncEmptyPage(p -> MessageEditData.fromContent(context.localize("error.tag.notfound")))
                .build();
        context.registerPage(page, true);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        AutoCompleteQuery option = event.getFocusedOption();
        if (option.getName().equals("language")) {
            event.replyChoices(Choice.toStringChoice(repositoryData.completeLanguage(option.getValue()))).queue();
            return;
        }
        if (option.getName().equals("category")) {
            event.replyChoices(tagData.completeCategories(option.getValue()).stream().map(e -> new Command.Choice(e.category(), e.id())).toList()).queue();
        }
    }
}
