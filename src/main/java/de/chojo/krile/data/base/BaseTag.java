/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.base;

import de.chojo.krile.data.dao.repository.tags.tag.Meta;
import de.chojo.krile.data.dao.repository.tags.tag.meta.TagType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.util.ArrayList;
import java.util.List;

public class BaseTag<M extends BaseMeta> {

    protected M meta;
    protected String tag;
    protected List<String> text;

    public BaseTag(String tag, List<String> text) {
        this.tag = tag;
        this.text = text;
    }
    public BaseTag(String tag, List<String> text, M meta) {
        this.tag = tag;
        this.text = text;
        this.meta = meta;
    }

    /**
     * Gets the text of the tag.
     *
     * @return The text of the tag as a string.
     */
    public String text() {
        if (meta.tagMeta().type() == TagType.EMBED) {
            DataObject dataObject = DataObject.fromJson(text.get(0));
            if (dataObject.hasKey("content")) {
                return dataObject.getString("content");
            }
            return "";
        }
        return text.get(0);
    }

    /**
     * Retrieves the list of message embeds from the text.
     *
     * @return A list of message embeds. If there are no embeds, the list will be empty.
     */
    public List<MessageEmbed> embeds() {
        List<MessageEmbed> embeds = new ArrayList<>();
        DataObject dataObject = DataObject.fromJson(text.get(0));
        if (dataObject.hasKey("embeds")) {
            var array = dataObject.getArray("embeds");
            for (int i = 0; i < array.length(); i++) {
                embeds.add(EmbedBuilder.fromData(array.getObject(i)).build());
            }
        } else {
            embeds.add(EmbedBuilder.fromData(dataObject).build());
        }
        return embeds;
    }

    /**
     * Retrieves the paged text.
     *
     * @return A list of paged text. If there is no paged text, the list will be empty.
     */
    public List<String> paged() {
        return text;
    }

    /**
     * Checks if the text is paged.
     *
     * @return True if the text is paged, false otherwise.
     */
    public boolean isPaged() {
        return text.size() != 1;
    }

    public M meta() {
        return meta;
    }

    public String tag() {
        return tag;
    }


}
