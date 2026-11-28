/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.tag;

import de.chojo.krile.commands.pagination.Pages;
import de.chojo.krile.commands.pagination.Pagination;
import de.chojo.krile.data.base.BaseTag;
import de.chojo.krile.data.dao.repository.tags.Tag;
import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode;

/**
 * Replies with the content of a tag, paged when the tag has several pages.
 * <p>
 * The info and delete buttons use plain custom ids ({@code tag_info:<tag id>}, {@code delete:<user id>}) that
 * {@link de.chojo.krile.service.TagInteractionListener} handles, so they keep working after a restart.
 */
public final class TagMessages {
    private TagMessages() {}

    /**
     * Shows a stored tag publicly, with its info button and, in a guild channel the bot can see, a delete button
     * for the user who called it.
     *
     * @param event event to reply to
     * @param tag   tag to show
     */
    public static void show(ReplyableEvent<?> event, Tag tag) {
        List<Button> buttons = new ArrayList<>();
        if (canDelete(event)) buttons.add(deleteButton(event.getUser()));
        buttons.add(infoButton(tag));
        show(event, tag, buttons, false);
    }

    /**
     * Shows a tag.
     *
     * @param event     event to reply to
     * @param tag       tag to show
     * @param buttons   buttons below the tag, may be empty
     * @param ephemeral whether only the user sees the tag
     */
    public static void show(ReplyableEvent<?> event, BaseTag<?> tag, List<Button> buttons, boolean ephemeral) {
        if (tag.isPaged()) {
            Pages pages = Pages.ofElements(tag.paged(), (text, builder, texts) -> page(tag, text, builder))
                    .ownedBy(event.getUser().getIdLong());
            Pagination.reply(event, pages, ephemeral, buttons);
            return;
        }
        MessageCreateBuilder builder = new MessageCreateBuilder().setAllowedMentions(Collections.emptyList());
        switch (tag.meta().tagMeta().type()) {
            case TEXT -> page(tag, tag.text(), builder);
            case EMBED -> {
                if (!tag.text().isBlank()) builder.setContent(tag.text());
                builder.setEmbeds(tag.embeds());
            }
        }
        if (!buttons.isEmpty()) builder.addComponents(ActionRow.of(buttons));
        event.with()
                .ephemeral(ephemeral)
                .builder(message -> message.applyData(builder.build()))
                .reply();
    }

    private static void page(BaseTag<?> tag, String text, MessageCreateBuilder builder) {
        builder.setContent(text).setAllowedMentions(Collections.emptyList());
        Optional.ofNullable(tag.meta().tagMeta().image())
                .ifPresent(image ->
                        builder.setEmbeds(new EmbedBuilder().setImage(image).build()));
    }

    private static boolean canDelete(ReplyableEvent<?> event) {
        if (event.getGuild() == null) return false;
        return event.getGuild()
                .getSelfMember()
                .hasPermission(event.jdaEvent().getGuildChannel(), Permission.VIEW_CHANNEL);
    }

    private static Button deleteButton(User user) {
        return Button.secondary("delete:" + user.getId(), fromUnicode("🗑️"));
    }

    private static Button infoButton(Tag tag) {
        return Button.secondary("tag_info:" + tag.id(), fromUnicode("❔"));
    }
}
