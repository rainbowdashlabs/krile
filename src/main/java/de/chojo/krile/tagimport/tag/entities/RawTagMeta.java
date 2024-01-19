/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.tagimport.tag.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import de.chojo.krile.data.dao.repository.tags.tag.meta.TagType;
import de.chojo.krile.tagimport.exception.ParsingException;
import de.chojo.krile.tagimport.tag.parsing.TagFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.chojo.krile.tagimport.tag.parsing.TagParser.MAPPER;
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
                         TagType type,
                         boolean enhanceMarkdown) {
    public static RawTagMeta createDefault(String id) {
        return new RawTagMeta(id, id, Collections.emptyList(), Collections.emptyList(), null, TagType.TEXT, true);
    }

    @JsonCreator
    public static RawTagMeta create(@JsonProperty("id") String id,
                                    @JsonProperty("tag") String tag,
                                    @JsonProperty("alias") List<String> alias,
                                    @JsonProperty("category") List<String> category,
                                    @JsonProperty("image") String image,
                                    @JsonProperty("type") TagType type,
                                    @JsonProperty("enhanceMarkdown") Boolean enhanceMarkdown) {
        return new RawTagMeta(id,
                tag,
                requireNonNullElse(alias, Collections.emptyList()),
                requireNonNullElse(category, Collections.emptyList()),
                image,
                requireNonNullElse(type, TagType.TEXT),
                requireNonNullElse(enhanceMarkdown, true));
    }


    /**
     * Injects values for id and tag into a RawTagMeta object.
     *
     * @param id  the value for the id property of the RawTagMeta object
     * @param tag the value for the tag property of the RawTagMeta object
     * @return a new RawTagMeta object with injected values for id and tag
     */
    public RawTagMeta inject(String id, String tag) {
        return new RawTagMeta(requireNonNullElse(this.id, id), requireNonNullElse(this.tag, tag), alias, category, image, type, enhanceMarkdown);
    }

    public static RawTagMeta parse(TagFile file, String defaultId, String defaultTag) throws ParsingException {
        if (file.meta().isPresent()) {
            try {
                return MAPPER.readValue(file.meta().get(), RawTagMeta.class)
                        .inject(defaultId, defaultTag);
            } catch (JsonProcessingException e) {
                throw new ParsingException("Failed to parse tag meta.%n%s".formatted(e.getMessage()), e);
            }
        }
        return RawTagMeta.createDefault(defaultId);
    }
}
