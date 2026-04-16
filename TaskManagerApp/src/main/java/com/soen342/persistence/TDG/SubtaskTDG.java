package com.soen342.persistence.TDG;

import com.soen342.model.Subtask;
import com.soen342.model.enums.TaskStatus;
import com.soen342.persistence.DBManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SubtaskTDG {
    public static final String TABLE_NAME = "subtasks";

    // catalog is the primary source for finding loaded entries by id, this method will return a freshly created entry
    public static Subtask find(int id) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "SELECT * FROM " + TABLE_NAME + " WHERE id = ?")) {
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                TaskStatus status;
                try {
                    status = TaskStatus.valueOf(rs.getString("status").toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Database corrupted, invalid status for subtask: " + id);
                }
                return new Subtask(
                        rs.getInt("id"),
                        rs.getString("title"),
                        status
                );
            }
        }
        throw new RuntimeException("Subtask not found: " + id);
    }

    public static void save(Subtask subtask, int taskId) throws SQLException {
        Integer collaboratorId = subtask.getLinkedCollaborator() == null ? null
                : subtask.getLinkedCollaborator().getCollaboratorId();

        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "INSERT INTO " + TABLE_NAME + " (id, title, status, task_id, collaborator_id) VALUES (?, ?, ?, ?, ?)")) {
            statement.setInt(1, subtask.getSubtaskId());
            statement.setString(2, subtask.getSubTitle());
            statement.setString(3, subtask.getSubStatus().name());
            statement.setInt(4, taskId);
            if (collaboratorId == null) statement.setNull(5, java.sql.Types.INTEGER);
            else statement.setInt(5, collaboratorId);
            statement.executeUpdate();
        }
    }

    public static void update(Subtask subtask) throws SQLException {
        Integer collaboratorId = subtask.getLinkedCollaborator() == null ? null
                : subtask.getLinkedCollaborator().getCollaboratorId();

        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "UPDATE " + TABLE_NAME + " SET title = ?, status = ?, collaborator_id = ? WHERE id = ?")) {
            statement.setString(1, subtask.getSubTitle());
            statement.setString(2, subtask.getSubStatus().name());
            if (collaboratorId == null) statement.setNull(3, java.sql.Types.INTEGER);
            else statement.setInt(3, collaboratorId);
            statement.setInt(4, subtask.getSubtaskId());
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