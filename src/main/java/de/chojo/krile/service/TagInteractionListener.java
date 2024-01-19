/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.service;

import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.LocaleProvider;
import de.chojo.krile.data.access.TagData;
import de.chojo.krile.data.dao.repository.tags.Tag;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A listener for tag button interactions.
 */
public class TagInteractionListener extends ListenerAdapter {
    private final ILocalizer localizer;
    private final TagData tags;

    public TagInteractionListener(ILocalizer localizer, TagData tags) {
        this.localizer = localizer;
        this.tags = tags;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String[] split = event.getButton().getId().split(":");
        if (split.length <= 2) return;
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
        Optional<Tag> byId = tags.byId(Integer.parseInt(id));
        if (byId.isEmpty()) {
            event.getHook().editOriginal(localizer.localize("error.tag.notfound", event.getGuild())).queue();
            return;
        }
        event.getHook().editOriginalEmbeds(byId.get().infoEmbed(localizer.context(LocaleProvider.guild(event.getGuild())))).queue();
    }
}
