package de.chojo.krile;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
                      @JsonProperty(defaultValue = "[]") List<String> alias,
                      @JsonProperty(defaultValue = "[]") List<String> category,
                      String image) {
    public static TagMeta createDefault(String id) {
        return new TagMeta(id, id, Collections.emptyList(), Collections.emptyList(), null);
    }

    public TagMeta inject(String id, String tag) {
        return new TagMeta(Objects.requireNonNullElse(this.id, id), Objects.requireNonNullElse(this.tag, tag), alias, category, image);
    }
}
