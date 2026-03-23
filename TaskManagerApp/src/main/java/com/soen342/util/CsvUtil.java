package com.soen342.util;

import com.soen342.catalog.*;
import com.soen342.model.*;
import com.soen342.model.enums.*;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Handles CSV import and export.
 * Column order: TaskName, Description, Subtask, Status, Priority,
 *               DueDate, ProjectName, ProjectDescription, Collaborator, CollaboratorCategory
 */
public class CsvUtil {

    private static final String HEADER =
        "TaskName,Description,Subtask,Status,Priority,DueDate," +
        "ProjectName,ProjectDescription,Collaborator,CollaboratorCategory";

    // -------------------------------------------------------------------------
    // EXPORT
    // -------------------------------------------------------------------------

    /**
     * Exports a list of tasks to a CSV file at the given directory path.
     */
    public static void exportToCSV(List<Task> tasks, String directoryPath) throws IOException {
        File dir = new File(directoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new FileNotFoundException("Directory not found: " + directoryPath);
        }

        File file = new File(dir, "tasks_export.csv");
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println(HEADER);
            for (Task task : tasks) {
                writeTaskRows(pw, task);
            }
        }
        System.out.println("[Export] Saved to: " + file.getAbsolutePath());
    }

    private static void writeTaskRows(PrintWriter pw, Task task) {
        String taskName    = escape(task.getTitle());
        String desc        = escape(task.getDescription());
        String status      = task.getStatus().name();
        String priority    = task.getPriority().name();
        String dueDate     = task.getDueDate() != null ? task.getDueDate().toString() : "";
        String projName    = task.getProject() != null ? escape(task.getProject().getName()) : "";
        String projDesc    = task.getProject() != null ? escape(task.getProject().getDescription()) : "";

        List<Subtask> subtasks = task.getSubtasks();

        if (subtasks.isEmpty()) {
            // One row with no subtask info
            pw.println(String.join(",",
                taskName, desc, "", status, priority, dueDate, projName, projDesc, "", ""));
        } else {
            for (Subtask sub : subtasks) {
                String subtaskTitle = escape(sub.getSubTitle());
                String collabName   = "";
                String collabCat    = "";
                if (sub.getLinkedCollaborator() != null) {
                    collabName = escape(sub.getLinkedCollaborator().getName());
                    collabCat  = sub.getLinkedCollaborator().getCategory().name();
                }
                pw.println(String.join(",",
                    taskName, desc, subtaskTitle, status, priority, dueDate,
                    projName, projDesc, collabName, collabCat));
            }
        }
    }

    // -------------------------------------------------------------------------
    // IMPORT
    // -------------------------------------------------------------------------

    /**
     * Imports tasks from a CSV file. Auto-creates missing projects, tags,
     * and collaborators as needed.
     */
    public static void importFromCSV(String filePath,
                                     TaskCatalog taskCatalog,
                                     ProjCatalog projCatalog,
                                     TagCatalog tagCatalog,
                                     CollaboratorCatalog collaboratorCatalog) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        int imported = 0;
        int skipped  = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // skip header
            if (line == null) return;

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] cols = parseCsvLine(line);
                if (cols.length < 5) { skipped++; continue; }

                try {
                    String taskName    = cols[0].trim();
                    String taskDesc    = cols.length > 1 ? cols[1].trim() : "";
                    String subtaskName = cols.length > 2 ? cols[2].trim() : "";
                    String statusStr   = cols.length > 3 ? cols[3].trim() : "OPEN";
                    String priorityStr = cols.length > 4 ? cols[4].trim() : "MEDIUM";
                    String dueDateStr  = cols.length > 5 ? cols[5].trim() : "";
                    String projName    = cols.length > 6 ? cols[6].trim() : "";
                    String projDesc    = cols.length > 7 ? cols[7].trim() : "";
                    String collabName  = cols.length > 8 ? cols[8].trim() : "";
                    String collabCat   = cols.length > 9 ? cols[9].trim() : "";

                    if (taskName.isEmpty()) { skipped++; continue; }

                    // Parse enums safely
                    Priority priority = parsePriority(priorityStr);
                    TaskStatus status = parseStatus(statusStr);

                    // Find or create the task (unique by title+dueDate)
                    LocalDate dueDate = null;
                    if (!dueDateStr.isEmpty()) {
                        try { dueDate = LocalDate.parse(dueDateStr); }
                        catch (DateTimeParseException ignored) {}
                    }

                    Task task;
                    final LocalDate finalDueDate = dueDate;
                    if (finalDueDate != null) {
                        task = taskCatalog.findByTitleAndDueDate(taskName, finalDueDate)
                                .orElse(null);
                    } else {
                        task = taskCatalog.getAllTasks().stream()
                                .filter(t -> t.getTitle().equalsIgnoreCase(taskName)
                                          && t.getDueDate() == null)
                                .findFirst().orElse(null);
                    }

                    if (task == null) {
                        task = taskCatalog.createTask(taskName, taskDesc, priority);
                        if (finalDueDate != null) task.setDueDate(finalDueDate);
                        switch (status) {
                            case CANCELLED -> task.cancel();
                            case COMPLETED -> task.complete();
                        }
                        imported++;
                    }

                    // Auto-create project if specified
                    if (!projName.isEmpty()) {
                        Project project = projCatalog.findOrCreate(projName, projDesc);
                        if (task.getProject() == null) {
                            project.addTask(task);
                        }
                    }

                    // Auto-create subtask + collaborator if specified
                    if (!subtaskName.isEmpty()) {
                        boolean alreadyExists = task.getSubtasks().stream()
                                .anyMatch(s -> s.getSubTitle().equalsIgnoreCase(subtaskName));
                        if (!alreadyExists) {
                            Subtask subtask = task.addSubtask(subtaskName);

                            if (!collabName.isEmpty()) {
                                CollaboratorCategory cat = parseCollaboratorCategory(collabCat);
                                Collaborator collab = collaboratorCatalog.findOrCreate(collabName, cat);
                                if (task.getProject() != null) {
                                    task.getProject().addCollaborator(collab);
                                }
                                try {
                                    collab.assignSubtask(subtask);
                                } catch (IllegalStateException e) {
                                    System.out.println("[Import Warning] " + e.getMessage());
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    System.out.println("[Import Warning] Skipped row: " + e.getMessage());
                    skipped++;
                }
            }
        }

        System.out.println("[Import] Done. Imported: " + imported + ", Skipped: " + skipped);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String escape(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    /**
     * Parses a CSV line, handling quoted fields with commas.
     */
    private static String[] parseCsvLine(String line) {
        java.util.List<String> result = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    private static Priority parsePriority(String s) {
        try { return Priority.valueOf(s.toUpperCase()); }
        catch (Exception e) { return Priority.MEDIUM; }
    }

    private static TaskStatus parseStatus(String s) {
        try { return TaskStatus.valueOf(s.toUpperCase()); }
        catch (Exception e) { return TaskStatus.OPEN; }
    }

    private static CollaboratorCategory parseCollaboratorCategory(String s) {
        try { return CollaboratorCategory.valueOf(s.toUpperCase()); }
        catch (Exception e) { return CollaboratorCategory.JUNIOR; }
    }
}
