package com.soen342.persistence.TDG;

import com.soen342.model.Tag;
import com.soen342.persistence.DBManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TagTDG {
    public static final String TABLE_NAME = "tags";

    public static Tag find(int id) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "SELECT * FROM " + TABLE_NAME + " WHERE id = ?")) {
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return new Tag(
                        rs.getInt("id"),
                        rs.getString("name")
                );
            }
        }
        throw new RuntimeException("Tag not found: " + id);
    }

    public static void save(Tag tag) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "INSERT INTO " + TABLE_NAME + " (id, name) VALUES (?, ?)")) {
            statement.setInt(1, tag.getTagId());
            statement.setString(2, tag.getName());
            statement.executeUpdate();
        }
    }

    public static void delete(int id) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "DELETE FROM " + TABLE_NAME + " WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }
}