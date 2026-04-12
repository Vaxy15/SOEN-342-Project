package com.soen342.persistence.TDG;

import com.soen342.model.Collaborator;
import com.soen342.model.enums.CollaboratorCategory;
import com.soen342.persistence.DBManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CollaboratorTDG {
    public static final String TABLE_NAME = "collaborators";

    //catalog is the primary source for finding loaded entries by id, this method will return a freshly created entry
    public static Collaborator find(int id) throws SQLException {

        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "select * from collaborators WHERE id = ?"
        )){
            statement.setInt(1, id);
            ResultSet queryResults = statement.executeQuery();

            while(queryResults.next()){
                int collId = queryResults.getInt("id");
                String name = queryResults.getString("name");
                String categoryRaw = queryResults.getString("category");

                CollaboratorCategory categoryEnum;
                try {
                    categoryEnum = CollaboratorCategory.valueOf(categoryRaw.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Database corrupted, invalid category: " + categoryRaw + " for collaborator: " + name);
                }
                return new Collaborator(id, name, categoryEnum);
            }
        }
        throw new RuntimeException("Collaborator not found: " + id);
    }

    public static void save(Collaborator collaborator) throws SQLException {

        int id = collaborator.getCollaboratorId();
        String name = collaborator.getName();
        String category = collaborator.getCategory().name();

        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "INSERT INTO " + TABLE_NAME+ " (id, name, category) VALUES (?, ?, ?)")) {
            statement.setInt(1, id);
            statement.setString(2, name);
            statement.setString(3, category);
            statement.executeUpdate();
        }
    }

    public static void update(Collaborator collaborator) throws SQLException {

        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "UPDATE "+TABLE_NAME+" SET name = ?, category = ? WHERE id = ?")) {
            statement.setString(1, collaborator.getName());
            statement.setString(2, collaborator.getCategory().name());
            statement.setInt(3, collaborator.getCollaboratorId());
            statement.executeUpdate();
        }
    }

    public static void delete(int id) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "DELETE FROM "+TABLE_NAME+" WHERE id = ?")) {
            statement.setInt(1, id);
        }
    }

    public static void linkToProject(int collaboratorId, int projectId) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "UPDATE " + TABLE_NAME + " SET project_id = ? WHERE id = ?")) {
            statement.setInt(1, projectId);
            statement.setInt(2, collaboratorId);
            statement.executeUpdate();
        }
    }

    public static void unlinkFromProject(int collaboratorId) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "UPDATE " + TABLE_NAME + " SET project_id = NULL WHERE id = ?")) {
            statement.setInt(1, collaboratorId);
            statement.executeUpdate();
        }
    }
}
