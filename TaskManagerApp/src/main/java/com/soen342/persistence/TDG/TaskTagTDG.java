package com.soen342.persistence.TDG;

import com.soen342.persistence.DBManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TaskTagTDG {
    public static final String TABLE_NAME = "task_tag_combination";

    public static void save(int taskId, int tagId) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "INSERT INTO " + TABLE_NAME + " (task_id, tag_id) VALUES (?, ?)")) {
            statement.setInt(1, taskId);
            statement.setInt(2, tagId);
            statement.executeUpdate();
        }
    }

    public static void delete(int taskId, int tagId) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "DELETE FROM " + TABLE_NAME + " WHERE task_id = ? AND tag_id = ?")) {
            statement.setInt(1, taskId);
            statement.setInt(2, tagId);
            statement.executeUpdate();
        }
    }
}