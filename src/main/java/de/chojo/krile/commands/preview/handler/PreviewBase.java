package de.chojo.krile.commands.preview.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.pagination.bag.PrivateListPageBag;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.data.base.BaseMeta;
import de.chojo.krile.data.base.BaseTag;
import de.chojo.krile.data.dao.repository.tags.tag.meta.TagMeta;
import de.chojo.krile.tagimport.exception.ParsingException;
import de.chojo.krile.tagimport.tag.RawTag;
import de.chojo.krile.tagimport.tag.entities.RawTagMeta;
import de.chojo.krile.tagimport.tag.parsing.TagFile;
import de.chojo.krile.tagimport.tag.parsing.TagParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


public abstract class PreviewBase implements SlashHandler {
    public void showTag(String text, IReplyCallback event, EventContext context) {
        TagFile tagFile = TagParser.parseTagFile(text);
        RawTagMeta meta;
        try {
            meta = RawTagMeta.parse(tagFile, "preview", "preview");
        } catch (ParsingException e) {
            event.reply(context.localize("error.repository.parsing", Replacement.create("error", e.getMessage()))).setEphemeral(true).queue();
            return;
        }
        BaseTag<BaseMeta> tag = new BaseTag<>("preview", RawTag.splitText(tagFile.content(), meta), new BaseMeta(new TagMeta(meta.image(), meta.type())));

        if (tag.isPaged()) {
            context.interactionHub().pageServices().registerPage(event, new PrivateListPageBag<>(tag.paged(), event.getUser().getIdLong()) {
                @Override
                public CompletableFuture<MessageEditData> buildPage() {
                    MessageEditBuilder builder = new MessageEditBuilder().setContent(currentElement()).setAllowedMentions(Collections.emptyList());
                    Optional.ofNullable(tag.meta().tagMeta().image()).ifPresent(image -> builder.setEmbeds(new EmbedBuilder().setImage(image).build()));
                    return CompletableFuture.completedFuture(builder.build());
                }
            }, true);
            return;
        }

        MessageCreateBuilder builder = new MessageCreateBuilder()
                .setAllowedMentions(Collections.emptyList());
        switch (tag.meta().tagMeta().type()) {
            case TEXT -> {
                builder.setContent(tag.text());
                Optional.ofNullable(tag.meta().tagMeta().image()).ifPresent(image -> builder.setEmbeds(new EmbedBuilder().setImage(image).build()));
            }
            case EMBED -> {
                if (!tag.text().isBlank()) builder.setContent(tag.text());
                builder.setEmbeds(tag.embeds());
            }
        }
        event.reply(builder.build()).setEphemeral(true).queue();
    }
}
