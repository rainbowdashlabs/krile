/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.tagimport.tag.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.chojo.krile.data.dao.repository.tags.tag.meta.TagType;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

/**
 * A record representing a tag.
 *
 * @param id       unique id of the tag
 * @param tag      tag name, either user defined or file name
 * @param alias    list of alias of the tag
 * @param category list of categories of the tag
 */
public record RawTagMeta(String id,
                         String tag,
                         List<String> alias,
                         List<String> category,
                         String image,
                         TagType type) {
    public static RawTagMeta createDefault(String id) {
        return new RawTagMeta(id, id, Collections.emptyList(), Collections.emptyList(), null, TagType.TEXT);
    }

    @JsonCreator
    public static RawTagMeta create(@JsonProperty("id") String id,
                                    @JsonProperty("tag") String tag,
                                    @JsonProperty("alias") List<String> alias,
                                    @JsonProperty("category") List<String> category,
                                    @JsonProperty("image") String image,
                                    @JsonProperty("type") TagType type) {
        return new RawTagMeta(id,
                tag,
                requireNonNullElse(alias, Collections.emptyList()),
                requireNonNullElse(category, Collections.emptyList()),
                image,
                requireNonNullElse(type, TagType.TEXT));
    }

    public RawTagMeta inject(String id, String tag) {
        return new RawTagMeta(requireNonNullElse(this.id, id), requireNonNullElse(this.tag, tag), alias, category, image, TagType.TEXT);
    }
}
