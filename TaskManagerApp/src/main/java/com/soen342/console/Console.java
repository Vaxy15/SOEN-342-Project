package com.soen342.console;

import com.soen342.catalog.*;
import com.soen342.model.*;
import com.soen342.model.enums.*;
import com.soen342.util.CalendarUtil;
import com.soen342.util.CsvUtil;
import com.soen342.util.ExportGateway;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Console is the top-level controller (facade) of the system.
 * It owns History, TaskCatalog, and ProjCatalog (composite aggregation).
 * All user interaction flows through this class.
 */
public class Console {

    // Owned catalogs (composite aggregation from domain model)
    private final TaskCatalog         taskCatalog         = new TaskCatalog();
    private final ProjCatalog         projCatalog         = new ProjCatalog();
    private final TagCatalog          tagCatalog          = new TagCatalog();
    private final CollaboratorCatalog collaboratorCatalog = new CollaboratorCatalog();

    private final Scanner scanner = new Scanner(System.in);

    // =========================================================================
    // MAIN LOOP
    // =========================================================================

    public void start() {
        System.out.println("========================================");
        System.out.println("  Personal Task Management System");
        System.out.println("  SOEN 342 - Winter 2026");
        System.out.println("========================================");

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1"  -> handleTaskMenu();
                case "2"  -> handleProjectMenu();
                case "3"  -> handleTagMenu();
                case "4"  -> handleCollaboratorMenu();
                case "5"  -> handleSearchMenu();
                case "6"  -> handleCsvMenu();
                case "7"  ->  handleICSMenu();
                case "0"  -> { running = false; System.out.println("Goodbye!"); }
                default   -> System.out.println("Invalid option.");
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n--- MAIN MENU ---");
        System.out.println("1. Tasks");
        System.out.println("2. Projects");
        System.out.println("3. Tags");
        System.out.println("4. Collaborators");
        System.out.println("5. Search & View Tasks");
        System.out.println("6. CSV Import / Export");
        System.out.println("7. ICS Export");
        System.out.println("0. Exit");
        System.out.print("Choice: ");
    }

    // =========================================================================
    // TASK MENU
    // =========================================================================

    private void handleTaskMenu() {
        System.out.println("\n--- TASKS ---");
        System.out.println("1. Create Task");
        System.out.println("2. View Task Details");
        System.out.println("3. Update Task");
        System.out.println("4. Complete Task");
        System.out.println("5. Cancel Task");
        System.out.println("6. Add Subtask");
        System.out.println("7. Mark Subtask Complete");
        System.out.println("8. Mark Subtask Cancelled");
        System.out.println("9. Remove Subtask");
        System.out.println("10. Add Tag to Task");
        System.out.println("11. Remove Tag from Task");
        System.out.println("12. Assign Task to Project");
        System.out.println("13. Remove Task from Project");
        System.out.println("14. View Activity History");
        System.out.println("15. Assign Collaborator to Task");
        System.out.print("Choice: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1"  -> createTask();
            case "2"  -> viewTaskDetails();
            case "3"  -> updateTask();
            case "4"  -> completeTask();
            case "5"  -> cancelTask();
            case "6"  -> addSubtask();
            case "7"  -> markSubtaskComplete();
            case "8"  -> markSubtaskCancelled();
            case "9"  -> removeSubtask();
            case "10"  -> addTagToTask();
            case "11" -> removeTagFromTask();
            case "12" -> assignTaskToProject();
            case "13" -> removeTaskFromProject();
            case "14" -> viewActivityHistory();
            case "15" -> assignCollaboratorToTask();
            default   -> System.out.println("Invalid option.");
        }
    }

    // --- Create Task ---

    private void createTask() {
        System.out.print("Task name: ");
        String name = scanner.nextLine().trim();
        if (name.isBlank()) { System.out.println("[Error] Name is required."); return; }

        System.out.print("Description (optional): ");
        String desc = scanner.nextLine().trim();

        Priority priority = promptPriority();
        if (priority == null){
            System.out.println("[Error] Priority is required.");
            return;
        }

        try{
            Task task = taskCatalog.createTask(name, desc.isEmpty() ? null : desc, priority);

            // Optional due date
            System.out.print("Due date (yyyy-MM-dd, or blank): ");
            String dueDateStr = scanner.nextLine().trim();
            if (!dueDateStr.isBlank()) {
                try {
                    task.setDueDate(LocalDate.parse(dueDateStr));
                } catch (DateTimeParseException e) {
                    System.out.println("[Warning] Invalid date format, due date not set.");
                }
            }

            // Optional recurrence
            System.out.print("Add recurrence pattern? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                addRecurrencePattern(task);
            }

            // Optional tags (loop)
            System.out.print("Add tags? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                boolean addingTags = true;
                while (addingTags) {
                    System.out.print("Tag name (or blank to stop): ");
                    String tagName = scanner.nextLine().trim();
                    if (tagName.isBlank()) break;
                    Optional<Tag> tag = tagCatalog.findByName(tagName);
                    if (tag.isEmpty()) {
                        System.out.println("[Error] Tag '" + tagName + "' not found. Create it first.");
                    } else {
                        task.addTag(tag.get());
                        System.out.println("[OK] Tag added.");
                    }
                }
            }
            System.out.println("[OK] Task created:\n" + task);
        } catch (IllegalArgumentException e) {
            System.out.println("[Error] " + e.getMessage());
            return;
        }
    }

    private void addRecurrencePattern(Task task) {
        System.out.println("Recurrence type: DAILY / WEEKLY / MONTHLY / CUSTOM");
        System.out.print("Type: ");
        RecurrenceType type;
        try {
            type = RecurrenceType.valueOf(scanner.nextLine().trim().toUpperCase());
        } catch (Exception e) {
            System.out.println("[Error] Invalid type.");
            return;
        }

        System.out.print("Interval (e.g., 1 for every 1 day/week/month): ");
        int interval;
        try { interval = Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("[Error] Invalid interval."); return; }

        System.out.print("Start date (yyyy-MM-dd): ");
        LocalDate start;
        try { start = LocalDate.parse(scanner.nextLine().trim()); }
        catch (Exception e) { System.out.println("[Error] Invalid date."); return; }

        System.out.print("End date (yyyy-MM-dd): ");
        LocalDate end;
        try { end = LocalDate.parse(scanner.nextLine().trim()); }
        catch (Exception e) { System.out.println("[Error] Invalid date."); return; }

        RecurrencePattern pattern = new RecurrencePattern(type, interval, start, end);

        if (type == RecurrenceType.WEEKLY) {
            System.out.print("Selected days (e.g., MON,WED,FRI): ");
            pattern.setSelectedDays(scanner.nextLine().trim());
        }
        if (type == RecurrenceType.MONTHLY) {
            System.out.print("Day of month (1-31): ");
            try { pattern.setDayOfMonth(Integer.parseInt(scanner.nextLine().trim())); }
            catch (NumberFormatException ignored) {}
        }

        task.setRecurrencePattern(pattern);
        System.out.println("[OK] Recurrence pattern set. "
                + task.getOccurrences().size() + " occurrences generated.");
    }

    // --- View Task Details ---

    private void viewTaskDetails() {
        Task task = promptTaskById();
        if (task == null) return;

        System.out.println("\n" + task);
        if (!task.getSubtasks().isEmpty()) {
            System.out.println("  Subtasks:");
            task.getSubtasks().forEach(System.out::println);
        }
        if (!task.getOccurrences().isEmpty()) {
            System.out.println("  Occurrences:");
            task.getOccurrences().forEach(System.out::println);
        }
    }

    // --- Update Task ---

    private void updateTask() {
        Task task = promptTaskById();
        if (task == null) return;

        System.out.print("New title (blank to keep '" + task.getTitle() + "'): ");
        String title = scanner.nextLine().trim();

        System.out.print("New description (blank to keep current): ");
        String desc = scanner.nextLine().trim();

        System.out.print("New priority (LOW/MEDIUM/HIGH, blank to keep " + task.getPriority() + "): ");
        String prioStr = scanner.nextLine().trim();
        Priority priority = prioStr.isBlank() ? null : parsePrioritySafe(prioStr);
        if (priority == null) {
            System.out.println("[Error] Invalid priority ignored");
        }

        System.out.print("New due date (yyyy-MM-dd, blank to keep): ");
        String dueDateStr = scanner.nextLine().trim();
        LocalDate dueDate = null;
        if (!dueDateStr.isBlank()) {
            try { dueDate = LocalDate.parse(dueDateStr); }
            catch (DateTimeParseException e) { System.out.println("[Warning] Invalid date ignored."); }
        }

        task.setTitle(title.isBlank() ? null : title);
        task.setDescription(desc.isBlank() ? null : desc);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        System.out.println("[OK] Task updated:\n" + task);
    }

    // --- Complete / Cancel ---

    private void completeTask() {
        Task task = promptTaskById();
        if (task == null) return;

        // Check if recurring — offer to complete single occurrence
        if (!task.isRecurring()) {
            System.out.println("This is a recurring task. Complete a specific occurrence? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                task.getOccurrences().forEach(System.out::println);
                System.out.print("Occurrence ID: ");
                try {
                    int occId = Integer.parseInt(scanner.nextLine().trim());
                    task.markOccurrenceComplete(occId);
                    System.out.println("[OK] Occurrence #" + occId + " completed.");
                } catch (NumberFormatException e) {
                    System.out.println("[Error] Invalid ID.");
                } catch (IllegalArgumentException e) {
                    System.out.println("[Error] " + e.getMessage());
                }
                return;
            }
        }

        task.complete();
        System.out.println("[OK] Task completed.");
    }

    private void cancelTask() {
        Task task = promptTaskById();
        if (task == null) return;
        task.cancel();
        System.out.println("[OK] Task cancelled.");
    }

    // --- Subtask Operations ---

    private void addSubtask() {
        Task task = promptTaskById();
        if (task == null) return;
        System.out.print("Subtask title: ");
        String title = scanner.nextLine().trim();
        if (title.isBlank()) { System.out.println("[Error] Title required."); return; }
        Subtask subtask = task.addSubtask(title);
        System.out.println("[OK] Subtask added: " + subtask);
    }

    private void markSubtaskComplete() {
        Task task = promptTaskById();
        if (task == null) return;
        List<Subtask> incompleteSubtasks = task.getSubtasks().stream().
                filter((s) -> s.getSubStatus() != TaskStatus.OPEN).toList();
        if (incompleteSubtasks.isEmpty()) { System.out.println("No incomplete subtasks found."); return; }
        incompleteSubtasks.forEach(System.out::println);
        System.out.print("Subtask ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            task.markSubtaskComplete(id);
            System.out.println("[OK] Subtask completed.");
            if (task.allSubtasksComplete())
                System.out.println("[Info] All subtasks done (parent task NOT auto-completed).");
        } catch (NumberFormatException e) {
            System.out.println("[Error] Invalid ID.");
        } catch (IllegalArgumentException e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private void markSubtaskCancelled() {
        Task task = promptTaskById();
        if (task == null) return;
        List<Subtask> uncancelledSubtasks = task.getSubtasks().stream().
                filter((s) -> s.getSubStatus() != TaskStatus.CANCELLED).toList();
        if (uncancelledSubtasks.isEmpty()) { System.out.println("No subtasks which can be cancelled found."); return; }
        uncancelledSubtasks.forEach(System.out::println);
        System.out.print("Subtask ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            task.markSubtaskCancelled(id);
            System.out.println("[OK] Subtask cancelled.");
            if (task.allSubtasksComplete())
                System.out.println("[Info] All subtasks done (parent task NOT auto-completed).");
        } catch (NumberFormatException e) {
            System.out.println("[Error] Invalid ID.");
        } catch (IllegalArgumentException e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private void removeSubtask() {
        Task task = promptTaskById();
        if (task == null) return;
        if (task.getSubtasks().isEmpty()) { System.out.println("No subtasks found."); return; }
        task.getSubtasks().forEach(System.out::println);
        System.out.print("Subtask ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            task.removeSubtask(id);
            System.out.println("[OK] Subtask removed.");
        } catch (NumberFormatException e) {
            System.out.println("[Error] Invalid ID.");
        } catch (IllegalArgumentException e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    // --- Tag Operations ---

    private void addTagToTask() {
        Task task = promptTaskById();
        if (task == null) return;
        System.out.print("Tag name: ");
        String name = scanner.nextLine().trim();
        tagCatalog.findByName(name).ifPresentOrElse(
            tag -> { task.addTag(tag); System.out.println("[OK] Tag added."); },
            () -> System.out.println("[Error] Tag not found. Create it first.")
        );
    }

    private void removeTagFromTask() {
        Task task = promptTaskById();
        if (task == null) return;
        System.out.print("Tag name: ");
        String name = scanner.nextLine().trim();
        tagCatalog.findByName(name).ifPresentOrElse(
            tag -> { task.removeTag(tag); System.out.println("[OK] Tag removed."); },
            () -> System.out.println("[Error] Tag not found.")
        );
    }

    // --- Project Assignment ---

    private void assignTaskToProject() {
        Task task = promptTaskById();
        if (task == null) return;
        System.out.print("Project name: ");
        String name = scanner.nextLine().trim();
        projCatalog.findByName(name).ifPresentOrElse(
            proj -> { proj.addTask(task); System.out.println("[OK] Task assigned to project."); },
            () -> System.out.println("[Error] Project not found.")
        );
    }

    private void removeTaskFromProject() {
        Task task = promptTaskById();
        if (task == null) return;
        if (task.getProject() == null) { System.out.println("Task is not in any project."); return; }
        task.getProject().removeTask(task);
        System.out.println("[OK] Task removed from project.");
    }

    // --- Collaborator Assignment ---

    private void assignCollaboratorToTask() {
        Task task = promptTaskById();
        if (task == null) return;

        if (task.getProject() == null) {
            System.out.println("[Error] Task must belong to a project to assign collaborators.");
            return;
        }

        System.out.print("Collaborator ID: ");
        try {
            int collabId = Integer.parseInt(scanner.nextLine().trim());
            Optional<Collaborator> collab = collaboratorCatalog.findById(collabId);
            if (collab.isEmpty()) {
                System.out.println("[Error] Collaborator ID not found.");
                return;
            }

            // Add collaborator to project if not already there
            task.getProject().addCollaborator(collab.get());

            Subtask linked = task.assignCollaborator(collab.get());
            System.out.println("[OK] Collaborator assigned. Linked subtask created: " + linked);
        } catch (NumberFormatException e) {
            System.out.println("[Error] Invalid ID.");
        } catch (IllegalStateException e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    // --- Activity History ---

    private void viewActivityHistory() {
        Task task = promptTaskById();
        if (task == null) return;
        History.printTaskHistory(task);
    }

    // =========================================================================
    // PROJECT MENU
    // =========================================================================

    private void handleProjectMenu() {
        System.out.println("\n--- PROJECTS ---");
        System.out.println("1. Create Project");
        System.out.println("2. Update Project");
        System.out.println("3. Delete Project");
        System.out.println("4. List All Projects");
        System.out.print("Choice: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1" -> createProject();
            case "2" -> updateProject();
            case "3" -> deleteProject();
            case "4" -> listProjects();
            default  -> System.out.println("Invalid option.");
        }
    }

    private void createProject() {
        System.out.print("Project name (must be unique): ");
        String name = scanner.nextLine().trim();
        if (name.isBlank()) { System.out.println("[Error] Name required."); return; }
        System.out.print("Description (optional): ");
        String desc = scanner.nextLine().trim();
        try {
            Project project = projCatalog.createProject(name, desc.isEmpty() ? null : desc);
            System.out.println("[OK] Project created: " + project);
        } catch (IllegalArgumentException e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private void updateProject() {
        System.out.print("Project name: ");
        String name = scanner.nextLine().trim();
        projCatalog.findByName(name).ifPresentOrElse(project -> {
            System.out.print("New name (blank to keep): ");
            String newName = scanner.nextLine().trim();
            System.out.print("New description (blank to keep): ");
            String newDesc = scanner.nextLine().trim();
            try {
                projCatalog.updateProject(project,
                    newName.isBlank() ? null : newName,
                    newDesc.isBlank() ? null : newDesc);
                System.out.println("[OK] Project updated: " + project);
            } catch (IllegalArgumentException e) {
                System.out.println("[Error] " + e.getMessage());
            }
        }, () -> System.out.println("[Error] Project not found."));
    }

    private void deleteProject() {
        System.out.print("Project name: ");
        String name = scanner.nextLine().trim();
        projCatalog.findByName(name).ifPresentOrElse(project -> {
            projCatalog.deleteProject(project);
            System.out.println("[OK] Project deleted.");
        }, () -> System.out.println("[Error] Project not found."));
    }

    private void listProjects() {
        List<Project> projects = projCatalog.getAllProjects();
        if (projects.isEmpty()) { System.out.println("No projects found."); return; }
        projects.forEach(System.out::println);
    }

    // =========================================================================
    // TAG MENU
    // =========================================================================

    private void handleTagMenu() {
        System.out.println("\n--- TAGS ---");
        System.out.println("1. Create Tag");
        System.out.println("2. List All Tags");
        System.out.print("Choice: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1" -> {
                System.out.print("Tag name: ");
                String name = scanner.nextLine().trim();
                if (name.isBlank()) { System.out.println("[Error] Name required."); return; }
                try {
                    Tag tag = tagCatalog.createTag(name);
                    System.out.println("[OK] Tag created: " + tag);
                } catch (IllegalArgumentException e) {
                    System.out.println("[Error] " + e.getMessage());
                }
            }
            case "2" -> {
                List<Tag> tags = tagCatalog.getAllTags();
                if (tags.isEmpty()) System.out.println("No tags found.");
                else tags.forEach(System.out::println);
            }
            default -> System.out.println("Invalid option.");
        }
    }

    // =========================================================================
    // COLLABORATOR MENU
    // =========================================================================

    private void handleCollaboratorMenu() {
        System.out.println("\n--- COLLABORATORS ---");
        System.out.println("1. Create Collaborator");
        System.out.println("2. List All Collaborators");
        System.out.print("Choice: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1" -> {
                System.out.print("Name: ");
                String name = scanner.nextLine().trim();
                System.out.println("Category (SENIOR/INTERMEDIATE/JUNIOR): ");
                System.out.print("Category: ");
                CollaboratorCategory cat;
                try { cat = CollaboratorCategory.valueOf(scanner.nextLine().trim().toUpperCase()); }
                catch (Exception e) { System.out.println("[Error] Invalid category."); return; }
                Collaborator c = collaboratorCatalog.createCollaborator(name, cat);
                System.out.println("[OK] Collaborator created: " + c);
            }
            case "2" -> {
                List<Collaborator> list = collaboratorCatalog.getAllCollaborators();
                if (list.isEmpty()) System.out.println("No collaborators found.");
                else list.forEach(System.out::println);
            }
            default -> System.out.println("Invalid option.");
        }
    }

    // =========================================================================
    // SEARCH MENU
    // =========================================================================

    private void handleSearchMenu() {
        System.out.println("\n--- SEARCH & VIEW TASKS ---");
        List<Task> results = taskSearch();

        if (results.isEmpty()) {
            System.out.println("\nNo tasks found matching your criteria.");
        } else {
            System.out.println("\n--- Results (" + results.size() + " tasks) ---");
            results.forEach(System.out::println);
        }

        // Optional CSV export
        System.out.print("\nExport results to CSV? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.print("Directory path: ");
            String dir = scanner.nextLine().trim();
            try {
                CsvUtil.exportToCSV(results, dir);
            } catch (IOException e) {
                System.out.println("[Error] " + e.getMessage());
            }
        }
    }

    private List<Task> taskSearch(){

        System.out.println("(Press Enter on any filter to skip it)");

        TaskCatalog.SearchCriteria criteria = new TaskCatalog.SearchCriteria();

        System.out.print("Keyword (title/description match): ");
        String kw = scanner.nextLine().trim();
        if (!kw.isBlank()) criteria.keyword(kw);

        System.out.print("Status (OPEN/COMPLETED/CANCELLED): ");
        String statusStr = scanner.nextLine().trim();
        if (!statusStr.isBlank()) {
            try { criteria.status(TaskStatus.valueOf(statusStr.toUpperCase())); }
            catch (Exception e) { System.out.println("[Warning] Invalid status ignored."); }
        }

        System.out.print("Priority (LOW/MEDIUM/HIGH): ");
        String prioStr = scanner.nextLine().trim();
        if (!prioStr.isBlank()) {
            Priority p = parsePrioritySafe(prioStr);
            if (p != null) criteria.priority(p);
        }

        System.out.print("Project name: ");
        String projName = scanner.nextLine().trim();
        if (!projName.isBlank()) {
            projCatalog.findByName(projName).ifPresentOrElse(
                    criteria::project,
                    () -> System.out.println("[Warning] Project not found, filter ignored.")
            );
        }

        System.out.print("Tag name: ");
        String tagName = scanner.nextLine().trim();
        if (!tagName.isBlank()) {
            tagCatalog.findByName(tagName).ifPresentOrElse(
                    criteria::tag,
                    () -> System.out.println("[Warning] Tag not found, filter ignored.")
            );
        }

        System.out.print("From date (yyyy-MM-dd): ");
        String fromStr = scanner.nextLine().trim();
        System.out.print("To date (yyyy-MM-dd): ");
        String toStr = scanner.nextLine().trim();
        if (!fromStr.isBlank() && !toStr.isBlank()) {
            try { criteria.dateRange(LocalDate.parse(fromStr), LocalDate.parse(toStr)); }
            catch (DateTimeParseException e) { System.out.println("[Warning] Invalid date range ignored."); }
        }

        System.out.print("Day of week (MON/TUE/WED/THU/FRI/SAT/SUN): ");
        String dowStr = scanner.nextLine().trim();
        if (!dowStr.isBlank()) {
            try { criteria.dayOfWeek(DayOfWeek.valueOf(dowStr.toUpperCase())); }
            catch (Exception e) { System.out.println("[Warning] Invalid day of week ignored."); }
        }

        System.out.print("Order by (duedate/priority/status/title, default=duedate): ");
        String order = scanner.nextLine().trim();
        if (!order.isBlank()) criteria.orderBy(order.toLowerCase());

        return taskCatalog.search(criteria);
    }

    // =========================================================================
    // CSV MENU
    // =========================================================================

    private void handleCsvMenu() {
        System.out.println("\n--- CSV ---");
        System.out.println("1. Import from CSV");
        System.out.println("2. Export all tasks to CSV");
        System.out.print("Choice: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1" -> {
                System.out.print("File path: ");
                String path = scanner.nextLine().trim();
                try {
                    CsvUtil.importFromCSV(path, taskCatalog, projCatalog,
                                          tagCatalog, collaboratorCatalog);
                } catch (IOException e) {
                    System.out.println("[Error] " + e.getMessage());
                }
            }
            case "2" -> {
                System.out.print("Directory path: ");
                String dir = scanner.nextLine().trim();
                try {
                    CsvUtil.exportToCSV(taskCatalog.getAllTasks(), dir);
                } catch (IOException e) {
                    System.out.println("[Error] " + e.getMessage());
                }
            }
            default -> System.out.println("Invalid option.");
        }
    }

    // =========================================================================
    // ICS EXPORT Menu
    // =========================================================================

    private void handleICSMenu() {
        System.out.println("\n--- ICS EXPORT ---");
        System.out.println("1. Export Single task");
        System.out.println("2. Export all tasks");
        System.out.println("3. Export task search");
        System.out.print("Choice: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1" -> {
               handleExportSingleTask();
            }
            case "2" -> {
               handleExportAllTasks();
            }
            case "3" -> {
               handleICSExportFromSearch();
            }
            default -> System.out.println("Invalid option.");
        }
    }

    private void handleExportSingleTask(){
        Task task = promptTaskById();
        if (task == null) return;
        System.out.println(task);
        ICSExport(List.of(task));
    }

    private void handleExportAllTasks(){

        List<Task> tasks = taskCatalog.getAllTasks();
        if (tasks.isEmpty()) {
            System.out.println("\nNo tasks found.");
            return;
        }
        ICSExport(tasks);
    }

    private void handleICSExportFromSearch() {
        System.out.println("\n--- SEARCH TASKS ---");
        List<Task> tasks = taskSearch();
        if (tasks.isEmpty()) {
            System.out.println("\nNo tasks found matching your criteria.");
            return;
        }

        System.out.println("\n--- Results (" + tasks.size() + " tasks) ---");
        tasks.forEach(System.out::println);
        System.out.println("\ncontinue to export? (y/n):");
        if (scanner.nextLine().trim().equalsIgnoreCase("y"))
            ICSExport(tasks);
    }

    private void ICSExport(List<Task> tasks) {

        String cwd = System.getProperty("user.dir");
        System.out.printf("file path (relative to %s) :", cwd);
        String path = scanner.nextLine().trim();
        if (path.isBlank()) {
            System.out.println("[Error] File path required.");
            return;
        }
        if (!path.endsWith(".ics")) {
            path += ".ics";
        }

        System.out.println("Exporting...");
        ExportGateway exportUtil = new CalendarUtil();
        try{
            exportUtil.exportTasksICS(tasks,path);
        } catch (FileAlreadyExistsException e) {
            System.out.println("[Error] File already exists.");
        } catch (IOException e) {
            System.out.println("[Error] " + e.getMessage());
        }
        System.out.println("Successfully exported to " + path + "!");
    }

    // =========================================================================
    // SHARED HELPERS
    // =========================================================================

    private Task promptTaskById() {
        //TODO print ids before scan so user knows what hes selecting
        System.out.print("Task ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<Task> task = taskCatalog.findById(id);
            if (task.isEmpty()) System.out.println("[Error] Task ID not found.");
            return task.orElse(null);
        } catch (NumberFormatException e) {
            System.out.println("[Error] Invalid ID.");
            return null;
        }
    }

    private Priority promptPriority() {
        System.out.print("Priority (LOW/MEDIUM/HIGH): ");
        Priority p = parsePrioritySafe(scanner.nextLine().trim());
        if (p == null) System.out.println("[Error] Invalid priority.");
        return p;
    }

    private Priority parsePrioritySafe(String s) {
        try { return Priority.valueOf(s.toUpperCase()); }
        catch (Exception e) { return null; }
    }
}
