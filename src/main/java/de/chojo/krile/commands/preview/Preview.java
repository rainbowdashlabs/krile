/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.preview;

import de.chojo.krile.commands.tag.TagMessages;
import de.chojo.krile.data.base.BaseMeta;
import de.chojo.krile.data.base.BaseTag;
import de.chojo.krile.data.dao.repository.tags.tag.meta.TagMeta;
import de.chojo.krile.tagimport.exception.ParsingException;
import de.chojo.krile.tagimport.tag.RawTag;
import de.chojo.krile.tagimport.tag.entities.RawTagMeta;
import de.chojo.krile.tagimport.tag.parsing.TagFile;
import de.chojo.krile.tagimport.tag.parsing.TagParser;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.CommandConfig;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Modal;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ModalEvent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.interactions.InteractionContextType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

/**
 * Shows how a tag file renders, without adding it to a repository. Only the user sees the preview.
 */
@Interaction("preview")
@CommandConfig(context = {InteractionContextType.GUILD, InteractionContextType.BOT_DM})
public class Preview {
    private static final HttpClient CLIENT = HttpClient.newBuilder().build();

    @Command(value = "modal", desc = "Create a preview by using a modal as input")
    public void modal(CommandEvent event) {
        event.replyModal(
                "onTag",
                Label.of(
                        event.resolve("command-preview-modal-modal-field-label"),
                        TextInput.of("tag", TextInputStyle.PARAGRAPH)));
    }

    @Modal("command-preview-modal-modal-label")
    public void onTag(ModalEvent event) {
        show(event, event.value("tag").getAsString());
    }

    @Command(value = "url", desc = "Use a file from an url")
    public void url(CommandEvent event, @Param(name = "url", value = "Url to download") String url) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            event.with().ephemeral(true).reply("error-url-invalidsource");
            return;
        }
        String body;
        try {
            body = CLIENT.sendAsync(
                            HttpRequest.newBuilder(uri).GET().build(),
                            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                    .orTimeout(5, TimeUnit.SECONDS)
                    .join()
                    .body();
        } catch (Exception e) {
            event.with().ephemeral(true).reply("error-repository-import");
            return;
        }
        show(event, body);
    }

    private static void show(ReplyableEvent<?> event, String text) {
        TagFile tagFile = TagParser.parseTagFile(text);
        RawTagMeta meta;
        try {
            meta = RawTagMeta.parse(tagFile, "preview", "preview");
        } catch (ParsingException e) {
            event.with().ephemeral(true).reply("error-repository-parsing", entry("error", e.getMessage()));
            return;
        }
        BaseTag<BaseMeta> tag = new BaseTag<>(
                "preview",
                RawTag.splitText(tagFile.content(), meta),
                new BaseMeta(new TagMeta(meta.image(), meta.type())));
        TagMessages.show(event, tag, List.of(), true);
    }
}
