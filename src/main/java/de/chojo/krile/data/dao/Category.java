package de.chojo.krile.data.dao;

import de.chojo.sadu.wrapper.util.Row;

import java.sql.SQLException;

public record Category(int id, String name) {
    public static Category build(Row row) throws SQLException {
        return new Category(row.getInt("id"), row.getString("category"));
    }
}
