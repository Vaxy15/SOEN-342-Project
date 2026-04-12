package com.soen342.persistence.TDG;

import com.soen342.model.ActivityEntry;
import com.soen342.model.enums.ActivityType;
import com.soen342.persistence.DBManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class ActivityEntryTDG {
    public static final String TABLE_NAME = "activity_entries";

    //catalog is the primary source for finding loaded entries by id, this method will return a freshly created entry
    public static ActivityEntry find(int id){

        ActivityEntry entry = null;
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "SELECT * FROM " + TABLE_NAME + " WHERE id = (id) VALUES (?)")) {
            statement.setInt(1, id);
            ResultSet queryResults = statement.executeQuery();
            while (queryResults.next()) {
                int entryId = queryResults.getInt("id");
                String description = queryResults.getString("description");
                String typeRaw = queryResults.getString("type");
                String timeStampRaw = queryResults.getString("timestamp");

                ActivityType typeEnum;
                try {
                    typeEnum = ActivityType.valueOf(typeRaw.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Database corrupted, invalid activity type: " + typeRaw + " for activity entry: " + id);
                }

                LocalDateTime timeStamp = null;
                try{
                    timeStamp = LocalDateTime.parse(timeStampRaw);
                } catch (DateTimeParseException e) {
                    throw new RuntimeException("Database corrupted, invalid timestamp: " + timeStampRaw + " for activity entry: " + id);
                }

                entry = new ActivityEntry(typeEnum, description, timeStamp);
            }
        } catch (SQLException e) {
            throw  new RuntimeException("SQL Error while loading database schema", e);
        }
        if(entry == null) throw new RuntimeException("Activity entry not found");
        return entry;
    }

    public static void save(ActivityEntry entry) throws SQLException{
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "INSERT INTO "+ TABLE_NAME +" (type, description, timestamp) VALUES (?, ?, ?)")) {
            statement.setString(1, entry.getType().name());
            statement.setString(2, entry.getDescription());
            statement.setString(3, entry.getTimeStamp().toString());

            statement.executeUpdate();
        }
    }

    public static void delete(ActivityEntry entry){
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "DELETE FROM " + TABLE_NAME + " WHERE type = ? AND description = ? AND timestamp = ?")) {
            statement.setString(1, entry.getType().name());
            statement.setString(2, entry.getDescription());
            statement.setString(3, entry.getTimeStamp().toString());
            statement.executeUpdate();
        } catch (SQLException e){
            throw  new RuntimeException("SQL Error while loading database schema", e);
        }
    }
}
