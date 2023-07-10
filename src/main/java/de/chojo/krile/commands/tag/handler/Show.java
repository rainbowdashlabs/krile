/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.tag.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.menus.MenuAction;
import de.chojo.jdautil.menus.MenuActionBuilder;
import de.chojo.jdautil.menus.entries.MenuEntry;
import de.chojo.jdautil.pagination.bag.PageButton;
import de.chojo.jdautil.pagination.bag.PrivateListPageBag;
import de.chojo.jdautil.util.Consumers;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.data.access.GuildData;
import de.chojo.krile.data.dao.TagGuild;
import de.chojo.krile.data.dao.repository.tags.Tag;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode;
import static org.slf4j.LoggerFactory.getLogger;

public class Show implements SlashHandler {
    private static final Logger log = getLogger(Show.class);
    private final GuildData guilds;

    public Show(GuildData guilds) {
        this.guilds = guilds;
    }

    public static void showTag(SlashCommandInteractionEvent event, EventContext context, TagGuild tagGuild, Tag tag) {
        tagGuild.tags().used(tag);
        showTag(event, context, tag);
    }

    public static void showTag(SlashCommandInteractionEvent event, EventContext context, Tag tag) {
        boolean delete = event.getGuild().getSelfMember().hasPermission(event.getChannel().asGuildMessageChannel(), Permission.VIEW_CHANNEL);
        if (tag.isPaged()) {
            context.registerPage(new PrivateListPageBag<>(tag.paged(), event.getUser().getIdLong()) {
                @Override
                public CompletableFuture<MessageEditData> buildPage() {
                    MessageEditBuilder builder = new MessageEditBuilder().setContent(currentElement());
                    tag.meta().image().ifPresent(image -> builder.setEmbeds(new EmbedBuilder().setImage(image).build()));
                    return CompletableFuture.completedFuture(builder.build());
                }

                @Override
                public List<PageButton> buttons() {
                    PageButton info = PageButton.of(p -> deleteButton(event.getUser()), (p, ctx) -> Consumers.empty());
                    if (!delete) return List.of(info);
                    return List.of(PageButton.of(p -> deleteButton(event.getUser()), (p, ctx) -> Consumers.empty()), info);
                }
            });
            return;
        }

        MessageCreateBuilder builder = new MessageCreateBuilder().setContent(tag.text());
        tag.meta().image().ifPresent(image -> builder.setEmbeds(new EmbedBuilder().setImage(image).build()));
        MenuActionBuilder menu = MenuAction.forCallback(builder.build(), event);
        if (delete) menu.addComponent(MenuEntry.of(deleteButton(event.getUser()), Consumers.empty()));
        menu.addComponent(MenuEntry.of(infoButton(tag), Consumers.empty()));
        context.registerMenu(menu.build());
    }

    private static Button deleteButton(User user) {
        return Button.secondary("delete:" + user.getId(), fromUnicode("🗑️"));
    }

    private static Button infoButton(Tag tag) {
        return Button.secondary("tag_info:" + tag.id(), fromUnicode("❔"));
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        String id = event.getOption("tag", OptionMapping::getAsString);
        event.deferReply().queue();
        Optional<Tag> tag = guilds.guild(event.getGuild()).tags().resolveTag(id);
        if (tag.isEmpty()) {
            event.getHook().editOriginal(context.localize("error.tag.notfound")).queue();
            event.getHook().deleteOriginal().queueAfter(1, TimeUnit.MINUTES);
            return;
        }
        showTag(event, context, guilds.guild(event), tag.get());
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        List<Command.Choice> choices = guilds.guild(event).tags()
                .complete(event.getFocusedOption().getValue())
                .stream()
                .map(e -> new Command.Choice(e.name(), e.id()))
                .toList();
        event.replyChoices(choices).queue();
    }
}
