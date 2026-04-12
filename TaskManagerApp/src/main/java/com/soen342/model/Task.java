package com.soen342.model;

import com.soen342.catalog.History;
import com.soen342.model.enums.ActivityType;
import com.soen342.model.enums.Priority;
import com.soen342.model.enums.TaskStatus;
import com.soen342.persistence.DBUtil;
import com.soen342.persistence.TDG.SubtaskTDG;
import com.soen342.persistence.TDG.TaskTDG;
import com.soen342.persistence.TDG.TaskTagTDG;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Task {

    private final int taskId;
    private String title;
    private String description;
    private final LocalDateTime createdOn;
    private LocalDate dueDate;
    private TaskStatus status;
    private Priority priority;
    private boolean isRecurring = false;

    private static History history;
    public static void setHistory(History history) {
        Task.history = history;
    }

    // Associations
    private Project project;                                  // 0..1
    private RecurrencePattern recurrencePattern;              // 0..1
    private final List<Subtask> subtasks = new ArrayList<>(); // 1 to *
    private final List<Tag> tags = new ArrayList<>();         // * to *
    private final List<ActivityEntry> activityHistory = new ArrayList<>();
    private final List<TaskOccurrence> occurrences = new ArrayList<>();

    public Task(int id,String title, String description, Priority priority) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Task title is required.");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Task priority is required.");
        }
        this.taskId = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = TaskStatus.OPEN;
        this.createdOn = LocalDateTime.now();

        recordActivity(ActivityType.CREATED, "Task '" + title + "' created.");
    }

    private Task(int id,String title, String description, Priority priority, TaskStatus status, boolean isRecurring, LocalDate dueDate, LocalDateTime createdOn) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Task title is required.");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Task priority is required.");
        }
        this.taskId = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.isRecurring = isRecurring;
        this.status = status;
        this.dueDate = dueDate;
        this.createdOn = createdOn;
    }

    // --- Status Operations ---

    public void complete() {
        if(StatusCheck(TaskStatus.COMPLETED)) return;
        TaskStatus prev = getStatus();
        this.status = TaskStatus.COMPLETED;
        try {
            TaskTDG.update(this);
        } catch (SQLException e) {
            this.status = prev;
            throw new RuntimeException("Failed to update task, operation aborted.", e);
        }
        recordActivity(ActivityType.COMPLETED, "Task '" + title + "' was completed.");
    }

    public void cancel() {
        if(StatusCheck(TaskStatus.CANCELLED)) return;
        TaskStatus prev = getStatus();
        this.status = TaskStatus.CANCELLED;
        try {
            TaskTDG.update(this);
        } catch (SQLException e) {
            this.status = prev;
            throw new RuntimeException("Failed to update task, operation aborted.", e);
        }
        recordActivity(ActivityType.CANCELLED, "Task '" + title + "' was cancelled.");
    }

    public void reopen() {
        if (StatusCheck(TaskStatus.OPEN)) return;
        TaskStatus prev = this.status;
        this.status = TaskStatus.OPEN;
        try {
            TaskTDG.update(this);
        } catch (SQLException e) {
            this.status = prev;
            throw new RuntimeException("Failed to update task, operation aborted.", e);
        }
        recordActivity(ActivityType.UPDATED, "Task '" + title + "' reopened.");
    }

    // --- Subtask Management ---

    public Subtask addSubtask(String subTitle) {
        Subtask subtask = new Subtask(subTitle);
        try {
            SubtaskTDG.save(subtask, this.taskId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save subtask, operation aborted.", e);
        }
        subtasks.add(subtask);
        recordActivity(ActivityType.UPDATED, "Subtask '" + subTitle + "' added.");
        return subtask;

    }

    public void removeSubtask(int subtaskId) {
        Subtask subtask = getSubtask(subtaskId);
        try {
            SubtaskTDG.delete(subtaskId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete subtask, operation aborted.", e);
        }
        subtasks.remove(subtask);
        recordActivity(ActivityType.UPDATED, "Subtask '" + subtask.getSubTitle() + "' removed.");
    }

    public void updateSubtaskTitle(int subtaskId, String newTitle) {
        Subtask subtask = getSubtask(subtaskId);
        subtask.setSubTitle(newTitle);
        recordActivity(ActivityType.UPDATED, "Subtask updated to '" + newTitle + "'.");
    }

    public void markSubtaskComplete(int subtaskId) {
        Subtask subtask = getSubtask(subtaskId);
        subtask.complete();
        recordActivity(ActivityType.UPDATED, "Subtask '" + subtask.getSubTitle() + "' cancelled.");
    }

    public void markSubtaskCancelled(int subtaskId) {
        Subtask subtask = getSubtask(subtaskId);
        subtask.cancel();
        recordActivity(ActivityType.UPDATED, "Subtask '" + subtask.getSubTitle() + "' completed.");
    }

    public void markSubtaskReopen(int subtaskId) {
        Subtask subtask = getSubtask(subtaskId);
        subtask.reopen();
        recordActivity(ActivityType.UPDATED, "Subtask '" + subtask.getSubTitle() + "' reopened.");
    }

    public boolean allSubtasksComplete() {
        return !subtasks.isEmpty() &&
               subtasks.stream().allMatch(s -> s.getSubStatus() == TaskStatus.COMPLETED);
    }

    public void markOccurrenceComplete(int occurrenceId) {
        TaskOccurrence occurrence = getOccurrence(occurrenceId);
        occurrence.complete();
        recordActivity(ActivityType.UPDATED, "Occurrence " + occurrence.getDueDate() + " completed.");
    }

    public void markOccurrenceCancelled(int occurrenceId) {
        TaskOccurrence occurrence = getOccurrence(occurrenceId);
        occurrence.cancel();
        recordActivity(ActivityType.UPDATED, "Occurrence " + occurrence.getDueDate() + " cancelled.");
    }

    // --- Tag Management ---

    public void addTag(Tag tag) {
        if (!tags.contains(tag)) {
            try {
                TaskTagTDG.save(this.taskId, tag.getTagId());
            } catch (SQLException e) {
                throw new RuntimeException("Failed to add tag, operation aborted.", e);
            }
            tags.add(tag);
            recordActivity(ActivityType.UPDATED, "Tag '" + tag.getName() + "' added.");
        }
    }

    public void removeTag(Tag tag) {
        try {
            TaskTagTDG.delete(this.taskId, tag.getTagId());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove tag, operation aborted.", e);
        }
        tags.remove(tag);
        recordActivity(ActivityType.UPDATED, "Tag '" + tag.getName() + "' removed.");
    }

    // --- Recurrence ---

    public void setRecurrencePattern(RecurrencePattern pattern) {
        this.isRecurring = true;
        this.dueDate = null;
        this.recurrencePattern = pattern;
        generateOccurrences();
        recordActivity(ActivityType.UPDATED, "Recurrence pattern set.");
        try {
            TaskTDG.update(this);
        } catch (SQLException e) {
            this.isRecurring = false;
            this.recurrencePattern = null;
            throw new RuntimeException("Failed to update task, operation aborted.", e);
        }
    }

    private void generateOccurrences() {
        occurrences.clear();
        if (recurrencePattern == null) return;
        for (LocalDate date : recurrencePattern.generateOccurrenceDates()) {
            occurrences.add(new TaskOccurrence(this, date));
        }
    }

    // --- Collaborator Assignment ---

    /**
     * Assigns a collaborator to this task by creating a linked subtask.
     * Validates capacity before assignment.
     */
    public Subtask assignCollaborator(Collaborator collaborator) {
        if (project == null) {
            throw new IllegalStateException("Collaborators can only be assigned to project tasks.");
        }
        if (!project.hasCollaborator(collaborator)) {
            throw new IllegalStateException(
                "Collaborator '" + collaborator.getName() + "' is not part of project '"
                + project.getName() + "'."
            );
        }
        // Validate capacity (throws if over limit)
        Subtask linked = new Subtask("Task for " + collaborator.getName());
        collaborator.assignSubtask(linked);
        try {
            SubtaskTDG.save(linked, this.taskId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save subtask, operation aborted.", e);
        }
        subtasks.add(linked);
        recordActivity(ActivityType.UPDATED, "Collaborator '" + collaborator.getName() + "' assigned via subtask.");
        return linked;
    }

    // --- Activity History ---

    private void recordActivity(ActivityType type, String description) {
        ActivityEntry entry = new ActivityEntry(type, "task #"+this.taskId+": "+description);
        history.record(entry);
        activityHistory.add(entry);
    }
    // --- Getters & Setters ---

    public int getTaskId() { return taskId; }

    public String getTitle() { return title; }

    public void setTitle(String title) {
        String oldTitle = this.title;
        this.title = title;
        try {
            TaskTDG.update(this);
        } catch (SQLException e) {
            this.title = oldTitle;
            throw new RuntimeException("Failed to update task, operation aborted.", e);
        }
        recordActivity(ActivityType.UPDATED, "Title updated.");
    }

    public String getDescription() { return description; }

    public void setDescription(String description) {
        String oldDescription = this.description;
        this.description = description;
        try {
            TaskTDG.update(this);
        } catch (SQLException e) {
            this.description = oldDescription;
            throw new RuntimeException("Failed to update task, operation aborted.", e);
        }
        recordActivity(ActivityType.UPDATED, "Description updated.");
    }

    public LocalDateTime getCreatedOn() { return createdOn; }

    public LocalDate getDueDate() { return dueDate; }

    public void setDueDate(LocalDate dueDate) {
        LocalDate oldDueDate = this.dueDate;
        RecurrencePattern oldPattern = this.recurrencePattern;
        boolean oldIsRecurring = this.isRecurring;
        this.dueDate = dueDate;
        this.recurrencePattern = null;
        this.isRecurring = false;
        try {
            TaskTDG.update(this);
        } catch (SQLException e) {
            this.dueDate = oldDueDate;
            this.recurrencePattern = oldPattern;
            this.isRecurring = oldIsRecurring;
            throw new RuntimeException("Failed to update task, operation aborted.", e);
        }
        recordActivity(ActivityType.UPDATED, "Due date set to " + dueDate + ", recurrence pattern removed.");
    }

    public TaskStatus getStatus() { return status; }

    public Priority getPriority() { return priority; }

    public void setPriority(Priority priority) {
        Priority oldPriority = this.priority;
        this.priority = priority;
        try {
            TaskTDG.update(this);
        } catch (SQLException e) {
            this.priority = oldPriority;
            throw new RuntimeException("Failed to update task, operation aborted.", e);
        }
        recordActivity(ActivityType.UPDATED, "Priority set to " + priority + ".");
    }

    public Project getProject() { return project; }

    //TODO check if project changing should be a responsibility of the task?
    public void setProject(Project project) {
        Project oldProject = this.project;
        this.project = project;
        try {
            TaskTDG.update(this);
        } catch (SQLException e) {
            this.project = oldProject;
            throw new RuntimeException("Failed to update task, operation aborted.", e);
        }
        if (project != null)
            recordActivity(ActivityType.UPDATED, "Project set to " + project.getName() + ".");
        else
            recordActivity(ActivityType.UPDATED, "Task removed from project.");
    }

    public boolean isRecurring() { return isRecurring; }

    public RecurrencePattern getRecurrencePattern() { return recurrencePattern; }

    public List<Subtask> getSubtasks() { return subtasks; }

    public List<Tag> getTags() { return tags; }

    public List<ActivityEntry> getActivityHistory() { return activityHistory; }

    public List<TaskOccurrence> getOccurrences() { return occurrences; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Task #").append(taskId).append(": ").append(title)
          .append(" [").append(status).append(", ").append(priority).append("]");
        if (dueDate != null) sb.append(" due ").append(dueDate);
        if (project != null) sb.append(" | Project: ").append(project.getName());
        if (!tags.isEmpty()) {
            sb.append(" | Tags: ");
            tags.forEach(t -> sb.append("#").append(t.getName()).append(" "));
        }
        if (recurrencePattern != null) sb.append(" | Recurring");
        if (!subtasks.isEmpty()) {
            sb.append("\n  Progress: ").append(subtasks.stream()
                .filter(s -> s.getSubStatus() == TaskStatus.COMPLETED).count())
              .append("/").append(subtasks.size()).append(" subtasks done");
        }
        return sb.toString();
    }

    //utils
    private Subtask getSubtask(int subtaskId) {
        List<Subtask> subtasksWithId = subtasks.stream().filter((s) -> s.getSubtaskId() == subtaskId).toList();
        if (subtasksWithId.isEmpty()) throw new IllegalArgumentException("Subtask id:" + subtaskId + ", not found.");
        return subtasksWithId.getFirst();
    }
    private TaskOccurrence getOccurrence(int occurrenceId) {
        List<TaskOccurrence> subtasksWithId = occurrences.stream().filter((o) -> o.getOccurrenceId() == occurrenceId).toList();
        if (subtasksWithId.isEmpty()) throw new IllegalArgumentException("Occurrence id:" + occurrenceId + ", not found.");
        return subtasksWithId.getFirst();
    }

    private boolean StatusCheck(TaskStatus status) {
        return this.status == status;
    }

    //raw data setting for db - skips business logic

    public static Task createTaskRaw(int id, String title, String description, Priority priority, TaskStatus status, boolean isRecurring, LocalDate dueDate, LocalDateTime createdOn) {
        return new Task(id, title, description, priority, status, isRecurring, dueDate, createdOn);
    }

    public Subtask addSubtaskRaw(int id, String subTitle, TaskStatus status) {
        Subtask subtask = new Subtask(id, subTitle, status);
        subtasks.add(subtask);
        return subtask;
    }

    public RecurrencePattern addRecurrenceRaw(RecurrencePattern recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
        this.isRecurring = true;
        return recurrencePattern;
    }

    public Tag addTagRaw(Tag tag) {
        this.tags.add(tag);
        return tag;
    }

    public ActivityEntry addActivityRaw(ActivityEntry activity) {
        this.activityHistory.add(activity);
        return activity;
    }

    public Project setProjectRaw(Project project) {
        this.project = project;
        return project;
    }
}
