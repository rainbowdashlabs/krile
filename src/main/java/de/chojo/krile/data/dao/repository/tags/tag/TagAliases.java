/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao.repository.tags.tag;

import de.chojo.krile.tagimport.tag.RawTag;
import org.intellij.lang.annotations.Language;

import java.util.List;

import static de.chojo.krile.data.bind.StaticQueryAdapter.builder;

public class TagAliases {
    private final Meta meta;
    private List<String> aliases;

    public TagAliases(Meta meta) {
        this.meta = meta;
    }

    /**
     * Updates a RawTag object in the repository.
     *
     * @param tag the RawTag object to be updated
     */
    public void update(RawTag tag) {
        // Clear repository aliases
        clear();

        for (String alias : tag.meta().alias()) {
            assign(alias);
        }
        aliases = tag.meta().alias();
    }

    /**
     * Assigns an alias to a tag.
     *
     * @param alias the alias to be assigned to the tag
     */
    public void assign(String alias) {
        @Language("postgresql")
        var insert = """
                INSERT INTO tag_alias(tag_id, alias) VALUES(?,?)""";
        builder().query(insert)
                .parameter(stmt -> stmt.setInt(meta.tag().id()).setString(alias))
                .insert()
                .sendSync();
    }

    /**
     * Clears the tag aliases associated with the current tag.
     * This method deletes all tag aliases from the "tag_alias" table
     * that have the same tag_id as the current tag.
     */
    public void clear() {
        builder().query("DELETE FROM tag_alias WHERE tag_id = ?")
                .parameter(stmt -> stmt.setInt(meta.tag().id()))
                .delete()
                .sendSync();
    }

    /**
     * Retrieves all aliases for the given tag.
     *
     * @return a {@link List} of {@link String} representing the aliases for the tag.
     */
    public List<String> all() {
        if (aliases == null) {

            @Language("postgresql")
            var select = """
                    SELECT alias FROM tag_alias WHERE tag_id = ?""";

            aliases = builder(String.class)
                    .query(select)
                    .parameter(stmt -> stmt.setInt(meta.tag().id()))
                    .readRow(row -> row.getString("alias"))
                    .allSync();
        }
        return aliases;
    }
}
