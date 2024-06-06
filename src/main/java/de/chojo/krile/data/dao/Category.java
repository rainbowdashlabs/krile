/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao;

import de.chojo.sadu.mapper.wrapper.Row;

import java.sql.SQLException;

public record Category(int id, String name) {
    public static Category build(Row row) throws SQLException {
        return new Category(row.getInt("id"), row.getString("category"));
    }
}
