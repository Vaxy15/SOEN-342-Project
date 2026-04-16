package com.soen342.persistence.TDG;

import com.soen342.model.Project;
import com.soen342.persistence.DBManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProjectTDG {
    public static final String TABLE_NAME = "projects";

    // catalog is the primary source for finding loaded entries by id, this method will return a freshly created entry
    public static Project find(int id) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "SELECT * FROM " + TABLE_NAME + " WHERE id = ?")) {
            statement.setInt(1, id);
            ResultSet queryResult = statement.executeQuery();
            if (queryResult.next()) {
                return new Project(
                        queryResult.getInt("id"),
                        queryResult.getString("name"),
                        queryResult.getString("description")
                );
            }
        }
        throw new RuntimeException("Project not found: " + id);
    }

    public static void save(Project project) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "INSERT INTO " + TABLE_NAME + " (id, name, description) VALUES (?, ?, ?)")) {
            statement.setInt(1, project.getProjectId());
            statement.setString(2, project.getName());
            statement.setString(3, project.getDescription());
            statement.executeUpdate();
        }
    }

    public static void update(Project project) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "UPDATE " + TABLE_NAME + " SET name = ?, description = ? WHERE id = ?")) {
            statement.setString(1, project.getName());
            statement.setString(2, project.getDescription());
            statement.setInt(3, project.getProjectId());
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
