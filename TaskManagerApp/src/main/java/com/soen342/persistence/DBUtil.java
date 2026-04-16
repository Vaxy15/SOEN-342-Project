package com.soen342.persistence;

import com.soen342.model.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DBUtil {

    /*public static void saveProject(Project proj) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "INSERT OR IGNORE INTO projects (id, name, description) VALUES (?, ?, ?)")) {
            statement.setInt(1,proj.getProjectId());
            statement.setString(2, proj.getName());
            statement.setString(3, proj.getDescription());
            statement.executeUpdate();
        }
    }

    public static void saveCollaborator(Collaborator collaborator) throws SQLException{

        int id = collaborator.getCollaboratorId();
        String name = collaborator.getName();
        String category = collaborator.getCategory().name();

        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "INSERT INTO collaborators (id, name, category) VALUES (?, ?, ?)")) {
            statement.setInt(1, id);
            statement.setString(2, name);
            statement.setString(3, category);
            statement.executeUpdate();
        }
    }

    public static void saveTag(Tag tag) throws SQLException{
        int id = tag.getTagId();
        String name = tag.getName();
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "INSERT INTO tags (id, name) VALUES (?, ?)")) {
            statement.setInt(1, id);
            statement.setString(2, name);
            statement.executeUpdate();
        }
    }

    public static void saveTask(Task task) throws SQLException{

        int id = task.getTaskId();
        String title = task.getTitle();
        String description = task.getDescription();
        String priority = task.getPriority().name();
        String status = task.getStatus().name();
        String dueDate = task.getDueDate() == null ? null : task.getDueDate().toString();
        int isRecurring = task.isRecurring() ? 1 : 0;
        String created_at = task.getCreatedOn().toString();

        Integer recurrencePatternId = null;
        if(task.isRecurring()) {
            RecurrencePattern recurrencePattern = task.getRecurrencePattern();
            String type = recurrencePattern.getType().name();
            int interval = recurrencePattern.getInterval();
            String startDate = recurrencePattern.getStartDate().toString();
            String endDate = recurrencePattern.getEndDate().toString();
            String selectedDays = recurrencePattern.getSelectedDays();
            Integer dayOfMonth = recurrencePattern.getDayOfMonth() == 0 ? null : recurrencePattern.getDayOfMonth();

            try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                    "INSERT INTO recurrence_patterns (type, interval, start_date, end_date, selected_days, day_of_month) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                statement.setString(1, type);
                statement.setInt(2, interval);
                statement.setString(3, startDate);
                statement.setString(4, endDate);
                statement.setString(5, selectedDays);
                if(dayOfMonth == null)
                    statement.setNull(6, java.sql.Types.INTEGER);
                else
                    statement.setInt(6, dayOfMonth);

                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        recurrencePatternId = generatedKeys.getInt(1);
                    } else {
                        throw new RuntimeException("Insert failed, no ID returned");
                    }
                }
            }
        }

        Integer projectId = task.getProject() == null ? null : task.getProject().getProjectId();

        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "INSERT INTO tasks (id, title, description, priority, status, due_date, is_recurring, created_at, recurrence_pattern_id, project_id)" +
                        " Values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        )) {
            statement.setInt(1, id);
            statement.setString(2, title);
            statement.setString(3, description);
            statement.setString(4, priority);
            statement.setString(5, status);
            statement.setString(6, dueDate);
            statement.setInt(7, isRecurring);
            statement.setString(8, created_at);
            if (recurrencePatternId == null)
                statement.setNull(9, java.sql.Types.INTEGER);
            else
                statement.setInt(9, recurrencePatternId);
            if (projectId == null)
                statement.setNull(10, java.sql.Types.INTEGER);
            else
                statement.setInt(10, projectId);
            statement.executeUpdate();
        }

        List<Subtask> subtasks = task.getSubtasks();
        if(subtasks.isEmpty()) return;

        for(Subtask subtask : subtasks) {
            int subtaskId = subtask.getSubtaskId();
            String subTitle = subtask.getSubTitle();
            String subStatus = subtask.getSubStatus().name();
            int taskId = task.getTaskId();
            Integer collaboratorId = subtask.getLinkedCollaborator() == null ? null : subtask.getLinkedCollaborator().getCollaboratorId();

            try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                    "INSERT INTO subtasks (id, title, status, task_id, collaborator_id) VALUES (?, ?, ?, ?, ?)"
            )) {
                statement.setInt(1, subtaskId);
                statement.setString(2, subTitle);
                statement.setString(3, subStatus);
                statement.setInt(4, taskId);
                if (collaboratorId == null)
                    statement.setNull(5, java.sql.Types.INTEGER);
                else
                    statement.setInt(5, collaboratorId);
                statement.executeUpdate();
            }
        }

        List<Tag> tags = task.getTags();
        if(tags.isEmpty()) return;
        for(Tag tag : tags) {
            int tagId = tag.getTagId();
            int taskId = task.getTaskId();
            try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                    "INSERT INTO task_tag_combination (task_id, tag_id) VALUES (?, ?)"
            )) {
                statement.setInt(1, taskId);
                statement.setInt(2, tagId);
                statement.executeUpdate();
            }
        }
    }

    public static void saveSubtask(Subtask subtask, int taskId) throws SQLException{
        int subtaskId = subtask.getSubtaskId();
        String subTitle = subtask.getSubTitle();
        String subStatus = subtask.getSubStatus().name();
        Integer collaboratorId = subtask.getLinkedCollaborator() == null ? null : subtask.getLinkedCollaborator().getCollaboratorId();

        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "INSERT INTO subtasks (id, title, status, task_id, collaborator_id) VALUES (?, ?, ?, ?, ?)"
        )) {
            statement.setInt(1, subtaskId);
            statement.setString(2, subTitle);
            statement.setString(3, subStatus);
            statement.setInt(4, taskId);
            if (collaboratorId == null)
                statement.setNull(5, java.sql.Types.INTEGER);
            else
                statement.setInt(5, collaboratorId);
            statement.executeUpdate();
        }
    }

    public static void saveActivityEntry(ActivityEntry entry) throws SQLException{
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "INSERT INTO activity_entries (type, description, timestamp) VALUES (?, ?, ?)")) {
            statement.setString(1, entry.getType().name());
            statement.setString(2, entry.getDescription());
            statement.setString(3, entry.getTimeStamp().toString());

            statement.executeUpdate();
        }
    }

    public static void editProject(Project proj) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "UPDATE projects SET name = ?, description = ? WHERE id = ?")) {
            statement.setString(1, proj.getName());
            statement.setString(2, proj.getDescription());
            statement.setInt(3, proj.getProjectId());
            statement.executeUpdate();
        }
    }

    public static void deleteProject(int projectId) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "DELETE FROM projects WHERE id = ?")) {
            statement.setInt(1, projectId);
            statement.executeUpdate();
        }
    }


    public static void editCollaborator(Collaborator collaborator) throws SQLException {

        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "UPDATE collaborators SET name = ?, category = ? WHERE id = ?")) {
            statement.setString(1, collaborator.getName());
            statement.setString(2, collaborator.getCategory().name());
            statement.setInt(3, collaborator.getCollaboratorId());
            statement.executeUpdate();
        }
    }

    public static void linkCollaboratorToProject(int collaboratorId, int projectId) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "UPDATE collaborators SET project_id = ? WHERE id = ?")) {
            statement.setInt(1, projectId);
            statement.setInt(2, collaboratorId);
            statement.executeUpdate();
        }
    }

    public static void unlinkCollaboratorFromProject(int collaboratorId) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "UPDATE collaborators SET project_id = NULL WHERE id = ?")) {
            statement.setInt(1, collaboratorId);
            statement.executeUpdate();
        }
    }

    public static void deleteCollaborator(int collaboratorId) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "DELETE FROM collaborators WHERE id = ?")) {
            statement.setInt(1, collaboratorId);
            statement.executeUpdate();
        }
    }


    public static void editTask(Task task) throws SQLException {
        Integer projectId = task.getProject() == null ? null : task.getProject().getProjectId();
        Integer recurrencePatternId = null;

        if (task.isRecurring()) {
            RecurrencePattern rp = task.getRecurrencePattern();
            Integer dayOfMonth = rp.getDayOfMonth() == 0 ? null : rp.getDayOfMonth();
            recurrencePatternId = rp.getPatternId();

            try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                    "UPDATE recurrence_patterns SET type = ?, interval = ?, start_date = ?, end_date = ?, selected_days = ?, day_of_month = ? WHERE id = ?")) {
                statement.setString(1, rp.getType().name());
                statement.setInt(2, rp.getInterval());
                statement.setString(3, rp.getStartDate().toString());
                statement.setString(4, rp.getEndDate().toString());
                statement.setString(5, rp.getSelectedDays());
                if (dayOfMonth == null)
                    statement.setNull(6, java.sql.Types.INTEGER);
                else
                    statement.setInt(6, dayOfMonth);
                statement.setInt(7, recurrencePatternId);
                statement.executeUpdate();
            }
        }

        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "UPDATE tasks SET title = ?, description = ?, priority = ?, status = ?, due_date = ?, is_recurring = ?, recurrence_pattern_id = ?, project_id = ? WHERE id = ?")) {
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

    public static void editSubtask(Subtask subtask) throws SQLException {
        Integer collaboratorId = subtask.getLinkedCollaborator() == null ? null : subtask.getLinkedCollaborator().getCollaboratorId();
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "UPDATE subtasks SET title = ?, status = ?, collaborator_id = ? WHERE id = ?")) {
            statement.setString(1, subtask.getSubTitle());
            statement.setString(2, subtask.getSubStatus().name());
            if (collaboratorId == null) statement.setNull(3, java.sql.Types.INTEGER);
            else statement.setInt(3, collaboratorId);
            statement.setInt(4, subtask.getSubtaskId());
            statement.executeUpdate();
        }
    }

    public static void deleteTask(int taskId) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "DELETE FROM recurrence_patterns WHERE id = (SELECT recurrence_pattern_id FROM tasks WHERE id = ?)")) {
            statement.setInt(1, taskId);
            statement.executeUpdate();
        }
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "DELETE FROM tasks WHERE id = ?")) {
            statement.setInt(1, taskId);
            statement.executeUpdate();
        }
    }

    public static void deleteSubtask(int subtaskId) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "DELETE FROM subtasks WHERE id = ?")) {
            statement.setInt(1, subtaskId);
            statement.executeUpdate();
        }
    }

    public static void addTagToTask(int taskId, int tagId) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "INSERT INTO task_tag_combination (task_id, tag_id) VALUES (?, ?)")) {
            statement.setInt(1, taskId);
            statement.setInt(2, tagId);
            statement.executeUpdate();
        }
    }

    public static void removeTagFromTask(int taskId, int tagId) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "DELETE FROM task_tag_combination WHERE task_id = ? AND tag_id = ?")) {
            statement.setInt(1, taskId);
            statement.setInt(2, tagId);
            statement.executeUpdate();
        }
    }

    public static void deleteTag(int tagId) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "DELETE FROM tags WHERE id = ?")) {
            statement.setInt(1, tagId);
            statement.executeUpdate();
        }
    }*/
}
