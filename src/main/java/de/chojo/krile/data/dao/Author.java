/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao;

import de.chojo.sadu.mapper.wrapper.Row;

import java.sql.SQLException;

// TODO: Add profile link?
public record Author(int id, String name, String mail) {
    public static final Author NONE = new Author(-1, "none", "none");

    public static Author build(Row row) throws SQLException {
        return build(row, "id", "name", "mail");
    }

    public static Author build(Row row, String id, String name, String mail) throws SQLException {
        return new Author(row.getInt(id), row.getString(name), row.getString(mail));
    }
}
