package de.chojo.krile.data.dao;

import de.chojo.sadu.wrapper.util.Row;

import java.sql.SQLException;

// TODO: Add profile link?
public record Author(int id, String name, String mail) {

    public static Author build(Row row) throws SQLException {
        return new Author(row.getInt("id"), row.getString("name"), row.getString("mail"));
    }
}
