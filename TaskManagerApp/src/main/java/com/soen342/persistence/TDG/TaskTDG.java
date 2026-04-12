package com.soen342.persistence.TDG;

import com.soen342.model.Task;
import com.soen342.model.enums.Priority;
import com.soen342.model.enums.TaskStatus;
import com.soen342.persistence.DBManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskTDG {
    public static final String TABLE_NAME = "tasks";

    // catalog is the primary source for finding loaded entries by id, this method will return a freshly created entry
    public static Task find(int id) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "SELECT * FROM " + TABLE_NAME + " WHERE id = ?")) {
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {

                return Task.createTaskRaw(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        Priority.valueOf(rs.getString("priority")),
                        TaskStatus.valueOf(rs.getString("status")),
                        rs.getInt("is_recurring") == 1,
                        rs.getString("due_date") == null ? null : LocalDate.parse(rs.getString("due_date")),
                        LocalDateTime.parse(rs.getString("created_at"))
                );
            }
        }
        throw new RuntimeException("Task not found: " + id);
    }

    public static void save(Task task) throws SQLException {
        Integer recurrencePatternId = null;
        if (task.isRecurring()) {
            RecurrencePatternTDG.save(task.getRecurrencePattern());
            recurrencePatternId = task.getRecurrencePattern().getPatternId();
        }

        Integer projectId = task.getProject() == null ? null : task.getProject().getProjectId();

        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "INSERT INTO " + TABLE_NAME +
                        " (id, title, description, priority, status, due_date, is_recurring, created_at, recurrence_pattern_id, project_id)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            statement.setInt(1, task.getTaskId());
            statement.setString(2, task.getTitle());
            statement.setString(3, task.getDescription());
            statement.setString(4, task.getPriority().name());
            statement.setString(5, task.getStatus().name());
            statement.setString(6, task.getDueDate() == null ? null : task.getDueDate().toString());
            statement.setInt(7, task.isRecurring() ? 1 : 0);
            statement.setString(8, task.getCreatedOn().toString());
            if (recurrencePatternId == null) statement.setNull(9, java.sql.Types.INTEGER);
            else statement.setInt(9, recurrencePatternId);
            if (projectId == null) statement.setNull(10, java.sql.Types.INTEGER);
            else statement.setInt(10, projectId);
            statement.executeUpdate();
        }
    }

    public static void update(Task task) throws SQLException {

        Integer recurrencePatternId = null;
        if (task.isRecurring()) {
            RecurrencePatternTDG.update(task.getRecurrencePattern());
            recurrencePatternId = task.getRecurrencePattern().getPatternId();
        }

        Integer projectId = task.getProject() == null ? null : task.getProject().getProjectId();

        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "UPDATE " + TABLE_NAME +
                        " SET title = ?, description = ?, priority = ?, status = ?, due_date = ?, is_recurring = ?, recurrence_pattern_id = ?, project_id = ?" +
                        " WHERE id = ?")) {
            statement.setString(1, task.getTitle());
            statement.setString(2, task.getDescription());
            statement.setString(3, task.getPriority().name());
            statement.setString(4, task.getStatus().name());
            statement.setString(5, task.getDueDate() == null ? null : task.getDueDate().toString());
            statement.setInt(6, task.isRecurring() ? 1 : 0);
            if (recurrencePatternId == null) statement.setNull(7, java.sql.Types.INTEGER);
            else statement.setInt(7, recurrencePatternId);
            if (projectId == null) statement.setNull(8, java.sql.Types.INTEGER);
            else statement.setInt(8, projectId);
            statement.setInt(9, task.getTaskId());
            statement.executeUpdate();
        }
    }

    public static void delete(int id) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "DELETE FROM recurrence_patterns WHERE id = (SELECT recurrence_pattern_id FROM " + TABLE_NAME + " WHERE id = ?)")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "DELETE FROM " + TABLE_NAME + " WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }
}