/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.discover;

import com.google.inject.Inject;
import de.chojo.krile.commands.pagination.Pages;
import de.chojo.krile.commands.pagination.Pagination;
import de.chojo.krile.commands.repository.RepositoryCompletion;
import de.chojo.krile.commands.tag.TagMessages;
import de.chojo.krile.configuration.Configuration;
import de.chojo.krile.data.access.CategoryData;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.data.access.TagData;
import de.chojo.krile.data.dao.Category;
import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.data.dao.repository.tags.Tag;
import de.chojo.krile.data.util.CompletedCategory;
import de.chojo.krile.data.util.RepositoryFilter;
import de.chojo.krile.data.util.TagFilter;
import de.chojo.krile.util.Texts;
import io.github.kaktushose.jdac.annotations.interactions.AutoComplete;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.CommandConfig;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.interactions.AutoCompleteEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;

import java.util.List;
import java.util.Optional;

/**
 * Finds public repositories and their tags across all guilds.
 */
@Interaction("discover")
@CommandConfig(context = {InteractionContextType.GUILD, InteractionContextType.BOT_DM})
public class Discover {
    private final RepositoryData repositoryData;
    private final TagData tagData;
    private final Configuration configuration;
    private final CategoryData categoryData;

    @Inject
    public Discover(
            RepositoryData repositoryData, TagData tagData, Configuration configuration, CategoryData categoryData) {
        this.repositoryData = repositoryData;
        this.tagData = tagData;
        this.configuration = configuration;
        this.categoryData = categoryData;
    }

    @Command(value = "repositories search", desc = "Search repositories")
    public void searchRepositories(
            CommandEvent event,
            @Param(name = "category", value = "category of the repository", optional = true) String category,
            @Param(name = "language", value = "language of the repository", optional = true) String language,
            @Param(name = "name", value = "pretty name of the repository", optional = true) String name,
            @Param(name = "platform", value = "platform of the repository", optional = true) String platform,
            @Param(name = "user", value = "username of the repository owner", optional = true) String user,
            @Param(name = "repository", value = "name of the repository", optional = true) String repository,
            @Param(name = "tags", value = "minimum amount of tags in a repository", optional = true) Integer tags) {
        Optional<Category> resolved = categoryData.resolve(category);
        if (resolved.isEmpty() && category != null) {
            event.with().ephemeral(true).reply("error-category-unknown");
            return;
        }
        List<Repository> found = repositoryData.search(new RepositoryFilter(
                resolved.map(Category::id).orElse(null), language, name, platform, user, repository, tags));
        if (found.isEmpty()) {
            event.with().ephemeral(true).reply("error-repository-notfound");
            return;
        }
        Pagination.reply(
                event,
                Pages.ofElements(found, (element, builder, texts) -> builder.setEmbeds(element.infoEmbed(texts))),
                true,
                List.of());
    }

    @AutoComplete(value = "searchRepositories", options = "category")
    public void completeRepositoryCategory(AutoCompleteEvent event) {
        replyCategories(event, repositoryData.completeCategories(event.getValue()));
    }

    @AutoComplete(value = "searchRepositories", options = "language")
    public void completeRepositoryLanguage(AutoCompleteEvent event) {
        RepositoryCompletion.completeStrings(event, repositoryData.completeLanguage(event.getValue()));
    }

    @AutoComplete(value = "searchRepositories", options = "platform")
    public void completePlatform(AutoCompleteEvent event) {
        RepositoryCompletion.completePlatform(event, configuration);
    }

    @AutoComplete(value = "searchRepositories", options = "user")
    public void completeUser(AutoCompleteEvent event) {
        RepositoryCompletion.completeStrings(event, repositoryData.completeUser(event.getValue()));
    }

    @AutoComplete(value = "searchRepositories", options = "repository")
    public void completeRepository(AutoCompleteEvent event) {
        RepositoryCompletion.completeStrings(event, repositoryData.completeRepository(event.getValue()));
    }

    @Command(value = "repositories random", desc = "Get a random repository")
    public void randomRepository(CommandEvent event) {
        Optional<Repository> random = repositoryData.random();
        if (random.isEmpty()) {
            event.with().ephemeral(true).reply("error-repository-notfound");
            return;
        }
        event.with()
                .ephemeral(true)
                .builder(message -> message.setEmbeds(random.get().infoEmbed(Texts.of(event))))
                .reply();
    }

    @Command(value = "tags search", desc = "Search tags")
    public void searchTags(
            CommandEvent event,
            @Param(name = "category", value = "category of the tag", optional = true) String category,
            @Param(name = "language", value = "language of the repository containing the tag", optional = true)
                    String language,
            @Param(name = "name", value = "Part of the tag name", optional = true) String name) {
        Optional<Category> resolved = categoryData.resolve(category);
        if (resolved.isEmpty() && category != null) {
            event.with().ephemeral(true).reply("error-category-unknown");
            return;
        }
        List<Tag> found =
                tagData.search(new TagFilter(resolved.map(Category::id).orElse(null), language, name));
        if (found.isEmpty()) {
            event.with().ephemeral(true).reply("error-tag-notfound");
            return;
        }
        Pagination.reply(
                event,
                Pages.ofElements(found, (element, builder, texts) -> builder.setEmbeds(element.infoEmbed(texts))),
                true,
                List.of());
    }

    @AutoComplete(value = "searchTags", options = "category")
    public void completeTagCategory(AutoCompleteEvent event) {
        replyCategories(event, tagData.completeCategories(event.getValue()));
    }

    @AutoComplete(value = "searchTags", options = "language")
    public void completeTagLanguage(AutoCompleteEvent event) {
        RepositoryCompletion.completeStrings(event, repositoryData.completeLanguage(event.getValue()));
    }

    @Command(value = "tags random", desc = "Get a random tag")
    public void randomTag(CommandEvent event) {
        Optional<Tag> random = tagData.random();
        if (random.isEmpty()) {
            event.with().ephemeral(true).reply("error-tag-notfound");
            return;
        }
        TagMessages.show(event, random.get());
        event.jdaEvent()
                .getHook()
                .sendMessageEmbeds(random.get().infoEmbed(Texts.of(event)))
                .setEphemeral(true)
                .queue();
    }

    private void replyCategories(AutoCompleteEvent event, List<CompletedCategory> categories) {
        event.replyChoices(categories.stream()
                .map(category -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(
                        category.category(), category.id()))
                .toList());
    }
}
