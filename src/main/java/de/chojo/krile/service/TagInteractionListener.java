/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.service;

import de.chojo.krile.data.access.TagData;
import de.chojo.krile.data.dao.repository.tags.Tag;
import de.chojo.krile.util.Texts;
import dev.goldmensch.fluava.Bundle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A listener for the info and delete buttons below tags.
 * <p>
 * The buttons use plain custom ids instead of JDA-Commands components, because those cannot carry the tag id or the
 * calling user and would stop working after a restart. JDA-Commands ignores custom ids it did not create.
 * <p>
 * The action and its value are the last two parts of the custom id, so ids from cjda-util, which carried a prefix
 * ({@code <menu>:delete:<user>}), work as well as the plain ones ({@code delete:<user>}).
 */
public class TagInteractionListener extends ListenerAdapter {
    private final Bundle texts;
    private final TagData tags;

    public TagInteractionListener(Bundle texts, TagData tags) {
        this.texts = texts;
        this.tags = tags;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String[] split = event.getButton().getCustomId().split(":");
        if (split.length < 2) return;
        String action = split[split.length - 2];
        if (action.equals("delete")) {
            delete(event, split[split.length - 1]);
            return;
        }
        if (action.equals("tag_info")) {
            info(event, split[split.length - 1]);
            return;
        }
    }

    public void delete(ButtonInteractionEvent event, String id) {
        if (!id.equals(event.getUser().getId())) {
            event.reply("❌").setEphemeral(true).queue();
            return;
        }
        event.deferEdit().queue();
        event.getMessage().delete().queue();
    }

    public void info(ButtonInteractionEvent event, String id) {
        event.deferReply().setEphemeral(true).queue();
        Texts texts = Texts.of(this.texts, event.getUserLocale().toLocale());
        Optional<Tag> byId = tags.byId(Integer.parseInt(id));
        if (byId.isEmpty()) {
            event.getHook().editOriginal(texts.get("error-tag-notfound")).queue();
            return;
        }
        event.getHook().editOriginalEmbeds(byId.get().infoEmbed(texts)).queue();
    }
}
