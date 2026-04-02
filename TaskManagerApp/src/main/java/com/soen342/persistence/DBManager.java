package com.soen342.persistence;

import com.soen342.catalog.*;
import com.soen342.model.*;
import com.soen342.model.enums.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO replace runtime exceptions with more specific runtime exceptions
//TODO should an error in the load/init function abort the program?
//TODO remember to setup db config so it enforces fk constraints

public class DBManager {
    private static final String DB_URL = "jdbc:sqlite:app.db";
    private static Connection connection;
    private static ProjCatalog projCatalog;
    private static CollaboratorCatalog collaboratorCatalog;
    private static TaskCatalog taskCatalog;
    private static TagCatalog tagCatalog;
    private static History history;

    static {
        try {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }

    public static void init() throws SQLException {

        try (InputStream in = DBManager.class.getResourceAsStream("/schema.sql")) {
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);

        } catch (IOException e){
            throw new RuntimeException("Cannot Initialize Database, IO Error while loading database schema", e);
        }
        catch (SQLException e){
            throw new RuntimeException("Cannot Initialize Database, SQL Error while loading database schema", e);
        }
    }

    public static void loadIntoCatalogs(
            ProjCatalog projCatalog,
            CollaboratorCatalog collaboratorCatalog,
            TaskCatalog taskCatalog,
            TagCatalog tagCatalog,
            History history
            ) throws SQLException {

        DBManager.projCatalog = projCatalog;
        DBManager.collaboratorCatalog = collaboratorCatalog;
        DBManager.taskCatalog = taskCatalog;
        DBManager.tagCatalog = tagCatalog;
        DBManager.history = history;
        // strict order, dont change
        loadProjects();
        loadCollaborators();
        loadTags();
        loadTasks();
        loadHistory();
    }

    public static void close() throws SQLException {
        connection.close();
    }


    public static void loadTasks(){

        Map<Integer, RecurrencePattern> recurrencePatterns = new HashMap<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet queryResults = statement.executeQuery("select * from recurrence_patterns ORDER BY id");

            while(queryResults.next()){

                int id = queryResults.getInt("id");
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

                RecurrencePattern recurrencePattern = RecurrencePattern.createRecurrencePatternRaw(id, typeEnum, interval, startDate, endDate, selectedDays, dayOfMonth);
                recurrencePatterns.put(id,recurrencePattern);
            }
        } catch (SQLException e){
            throw  new RuntimeException("SQL Error while loading database schema", e);
        }

        try {
            Statement statement = connection.createStatement();
            ResultSet queryResults = statement.executeQuery("select * from tasks ORDER BY id");
            while(queryResults.next()){
                int id = queryResults.getInt("id");
                String title = queryResults.getString("title");
                String description = queryResults.getString("description");
                String priorityRaw = queryResults.getString("priority");
                String statusRaw = queryResults.getString("status");
                String dueDateRaw = queryResults.getString("due_date");
                int isRecurringRaw = queryResults.getInt("is_recurring");
                String createdAtRaw = queryResults.getString("created_at");

                int recurrence_id = queryResults.getInt("recurrence_pattern_id");
                if (!queryResults.wasNull()) {
                    if(recurrencePatterns.get(recurrence_id) == null)
                        throw new RuntimeException("Database corrupted, task references non-existent recurrence id: " + recurrence_id);
                }

                int projectId = queryResults.getInt("project_id");
                if (!queryResults.wasNull()) {
                    projCatalog.findById(projectId).orElseThrow(
                            () -> new RuntimeException("Database corrupted, task references non-existent project id: " + projectId));
                }

                Priority priorityEnum;
                try{
                    priorityEnum = Priority.valueOf(priorityRaw.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Database corrupted, invalid priority: " + priorityRaw + " for task: " + id);
                }

                TaskStatus statusEnum;
                try{
                    statusEnum = TaskStatus.valueOf(statusRaw.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Database corrupted, invalid status: " + statusRaw + " for task: " + id);
                }

                LocalDate dueDate = null;
                if (dueDateRaw != null && !dueDateRaw.isEmpty()) {
                    dueDate = LocalDate.parse(dueDateRaw);
                }

                LocalDateTime createdAt = null;
                if (createdAtRaw != null && !createdAtRaw.isEmpty()) {
                    try{
                        createdAt = LocalDateTime.parse(createdAtRaw);
                    } catch (Exception e) {
                        throw new RuntimeException("Database corrupted, invalid created_at: " + createdAtRaw + " for task: " + id);
                    }
                }

                boolean isRecurring = isRecurringRaw == 1;

                RecurrencePattern recurrencePattern = recurrencePatterns.get(recurrence_id);
                Project Project = projCatalog.findById(projectId).orElse(null);

                Task task = taskCatalog.loadTaskFromDataStore(id, title, description, priorityEnum, statusEnum, isRecurring, dueDate, createdAt);
                if(isRecurring) {
                    task.addRecurrenceRaw(recurrencePattern);
                }
                if(Project != null) {
                    Project.addTaskRaw(task);
                    task.setProjectRaw(Project);
                }
            }
        } catch (SQLException e){
            throw  new RuntimeException("SQL Error while loading database schema", e);
        }

        try{
            Statement statement = connection.createStatement();
            ResultSet queryResults = statement.executeQuery("select * from subtasks");
            while(queryResults.next()){
                int id = queryResults.getInt("id");
                String title = queryResults.getString("title");
                String statusRaw = queryResults.getString("status");

                int task_id = queryResults.getInt("task_id");
                if(!queryResults.wasNull()) {
                    taskCatalog.findById(task_id).orElseThrow(
                            () -> new RuntimeException("Database corrupted, subtask references non-existent task id: " + task_id));
                }

                int collab_id = queryResults.getInt("collaborator_id");
                if (!queryResults.wasNull()) {
                    collaboratorCatalog.findById(collab_id).orElseThrow(
                            () -> new RuntimeException("Database corrupted, subtask references non-existent collaborator id: " + collab_id));
                }

                TaskStatus statusEnum;
                try{
                    statusEnum = TaskStatus.valueOf(statusRaw.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Database corrupted, invalid status: " + statusRaw + " for subtask: " + id);
                }

                Task parent = taskCatalog.findById(task_id).orElse(null);
                Subtask subtask = parent.addSubtaskRaw(id,title,statusEnum);
                Collaborator collaborator = collaboratorCatalog.findById(collab_id).orElse(null);
                if(collaborator != null){
                    collaborator.addSubtaskRaw(subtask);
                    subtask.setLinkedCollaboratorRaw(collaborator);
                }
            }
        }  catch (SQLException e){
            throw  new RuntimeException("SQL Error while loading database schema", e);
        }

        try{
            Statement statement = connection.createStatement();
            ResultSet queryResults = statement.executeQuery("select * from task_tag_combination");
            while(queryResults.next()){
                int task_id = queryResults.getInt("task_id");
                int tag_id = queryResults.getInt("tag_id");

                Task task = taskCatalog.findById(task_id).orElse(null);
                Tag tag = tagCatalog.findById(tag_id).orElse(null);

                if(task == null || tag == null) {
                    throw new RuntimeException("Database corrupted, task or tag references non-existent id");
                }
                task.addTagRaw(tag);
            }
        }  catch (SQLException e){
            System.out.println("IO Error while loading database schema");
        }
    }

    public static void loadCollaborators(){

        try {
            Statement statement = connection.createStatement();
            ResultSet queryResults = statement.executeQuery("select * from collaborators ORDER BY id");
            while(queryResults.next()){
                int id = queryResults.getInt("id");
                String name = queryResults.getString("name");
                String categoryRaw = queryResults.getString("category");
                int projectId = queryResults.getInt("project_id");

                if (!queryResults.wasNull()) {
                    projCatalog.findById(projectId)
                            .orElseThrow(() -> new RuntimeException("Database corrupted, collaborator references non-existent project id: " + projectId));

                }

                CollaboratorCategory categoryEnum;
                try {
                    categoryEnum = CollaboratorCategory.valueOf(categoryRaw.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Database corrupted, invalid category: " + categoryRaw + " for collaborator: " + name);
                }

                Collaborator collaborator = collaboratorCatalog.loadCollaboratorFromDataStore(id, name, categoryEnum);
                if(collaboratorCatalog.findById(id).isEmpty()){
                    throw new RuntimeException("Database corrupted, not in sync with project catalog");
                }

                Project Project = projCatalog.findById(projectId).orElse(null);
                if(Project != null) {
                    Project.addCollaboratorRaw(collaborator);
                }
            }
        } catch (SQLException e){
            throw  new RuntimeException("SQL Error while loading database schema", e);
        }
    }

    public static void loadTags(){
        try{
            Statement statement = connection.createStatement();
            ResultSet queryResults = statement.executeQuery("select * from tags ORDER BY id");
            while(queryResults.next()){
                int id = queryResults.getInt("id");
                String name = queryResults.getString("name");
                tagCatalog.loadTagFromDataStore(id, name);
            }
        }  catch (SQLException e){
            System.out.println("IO Error while loading database schema");
        }
    }

    public static void loadProjects(){

        try {
            Statement statement = connection.createStatement();
            ResultSet queryResults = statement.executeQuery("select * from projects ORDER BY id");
            while(queryResults.next()){
                int id = queryResults.getInt("id");
                String name = queryResults.getString("name");
                String description = queryResults.getString("description");
                projCatalog.loadProjFromDataStore(id, name, description);
                if(projCatalog.findById(id).isEmpty()){
                    throw new RuntimeException("Database corrupted, not in sync with project catalog");
                }
            }
        } catch (SQLException e){
            throw  new RuntimeException("SQL Error while loading database schema", e);
        }

    }

    public static void loadHistory(){

        try{
            Statement statement = connection.createStatement();
            ResultSet queryResults = statement.executeQuery("select * from activity_entries ORDER BY id");

            while(queryResults.next()){
                int id = queryResults.getInt("id");
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

                ActivityEntry entry = new ActivityEntry(typeEnum, description, timeStamp);


                Pattern p = Pattern.compile("#(\\d+)");
                Matcher m = p.matcher(description);

                if (m.find()) {
                    int taskId = Integer.parseInt(m.group(1));
                    Task task = taskCatalog.findById(taskId).orElse(null);
                    if(task != null) {
                        task.addActivityRaw(entry);
                    }
                }
                history.recordRaw(entry);
            }
        }  catch (SQLException e){
            throw  new RuntimeException("SQL Error while loading database schema", e);
        }
    }

}
