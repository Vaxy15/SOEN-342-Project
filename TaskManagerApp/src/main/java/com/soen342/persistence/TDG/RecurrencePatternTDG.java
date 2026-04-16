package com.soen342.persistence.TDG;

import com.soen342.model.RecurrencePattern;
import com.soen342.model.enums.RecurrenceType;
import com.soen342.persistence.DBManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class RecurrencePatternTDG {
    public static final String TABLE_NAME = "recurrence_patterns";

    // catalog is the primary source for finding loaded entries by id, this method will return a freshly created entry
    public static RecurrencePattern find(int id) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "select * from " + TABLE_NAME + " WHERE id = ?"
        )) {
            statement.setInt(1, id);
            ResultSet queryResults = statement.executeQuery();
            while (queryResults.next()) {

                int rec_id = queryResults.getInt("id");
                String typeRaw = queryResults.getString("type");
                int interval = queryResults.getInt("interval");
                String startDateRaw = queryResults.getString("start_date");
                String endDateRaw = queryResults.getString("end_date");
                String selectedDays = queryResults.getString("selected_days");
                int dayOfMonth = queryResults.getInt("day_of_month");

                LocalDate startDate = null;
                LocalDate endDate = null;
                if (startDateRaw != null && !startDateRaw.isEmpty()) {
                    startDate = LocalDate.parse(startDateRaw);
                }
                if (endDateRaw != null && !endDateRaw.isEmpty()) {
                    endDate = LocalDate.parse(endDateRaw);
                }

                RecurrenceType typeEnum;
                try {
                    typeEnum = RecurrenceType.valueOf(typeRaw.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Database corrupted, invalid recurrence type: " + typeRaw);
                }

                return RecurrencePattern.createRecurrencePatternRaw(id, typeEnum, interval, startDate, endDate, selectedDays, dayOfMonth);
            }
        }
        throw new RuntimeException("RecurrencePattern not found: " + id);
    }

    public static void save(RecurrencePattern recurrencePattern) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "INSERT INTO " + TABLE_NAME + " (id, type, interval, start_date, end_date, selected_days, day_of_month) VALUES (?, ?, ?, ?, ?, ?, ?)"
        )){
            int id = recurrencePattern.getPatternId();
            String type = recurrencePattern.getType().name();
            int interval = recurrencePattern.getInterval();
            String startDate = recurrencePattern.getStartDate().toString();
            String endDate = recurrencePattern.getEndDate().toString();
            String selectedDays = recurrencePattern.getSelectedDays();
            Integer dayOfMonth = recurrencePattern.getDayOfMonth() == 0 ? null : recurrencePattern.getDayOfMonth();

            statement.setInt(1, id);
            statement.setString(2, type);
            statement.setInt(3, interval);
            statement.setString(4, startDate);
            statement.setString(5, endDate);
            statement.setString(6, selectedDays);
            if(dayOfMonth == null)
                statement.setNull(7, java.sql.Types.INTEGER);
            else
                statement.setInt(7, dayOfMonth);
            statement.executeUpdate();
        }
    }

    public static void update(RecurrencePattern recurrencePattern) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "UPDATE " + TABLE_NAME + " SET type = ?, interval = ?, start_date = ?, end_date = ?, selected_days = ?, day_of_month = ? WHERE id = ?"
        )){
            int id = recurrencePattern.getPatternId();
            String type = recurrencePattern.getType().name();
            int interval = recurrencePattern.getInterval();
            String startDate = recurrencePattern.getStartDate().toString();
            String endDate = recurrencePattern.getEndDate().toString();
            String selectedDays = recurrencePattern.getSelectedDays();
            Integer dayOfMonth = recurrencePattern.getDayOfMonth() == 0 ? null : recurrencePattern.getDayOfMonth();
            statement.setString(1, type);
            statement.setInt(2, interval);
            statement.setString(3, startDate);
            statement.setString(4, endDate);
            statement.setString(5, selectedDays);
            if(dayOfMonth == null)
                statement.setNull(6, java.sql.Types.INTEGER);
            else
                statement.setInt(6, dayOfMonth);
            statement.setInt(7, id);
            statement.executeUpdate();
        }
    }

    public static void delete(int id) throws SQLException {
        try (PreparedStatement statement = DBManager.getConnection().prepareStatement(
                "DELETE FROM " + TABLE_NAME + " WHERE id = ?"
        )){
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }
}
