package de.chojo.krile.tag.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
public record TagMeta(String id,
                      String tag,
                       List<String> alias,
                      List<String> category,
                      String image) {
    public static TagMeta createDefault(String id) {
        return new TagMeta(id, id, Collections.emptyList(), Collections.emptyList(), null);
    }

    @JsonCreator
    public static TagMeta create(@JsonProperty("id") String id,
                                 @JsonProperty("tag") String tag,
                                 @JsonProperty("alias") List<String> alias,
                                 @JsonProperty("category") List<String> category,
                                 @JsonProperty("image") String image) {
        return new TagMeta(requireNonNull(id),
                requireNonNull(tag),
                requireNonNullElse(alias, Collections.emptyList()),
                requireNonNullElse(category, Collections.emptyList()),
                image);
    }

    public TagMeta inject(String id, String tag) {
        return new TagMeta(requireNonNullElse(this.id, id), requireNonNullElse(this.tag, tag), alias, category, image);
    }
}
