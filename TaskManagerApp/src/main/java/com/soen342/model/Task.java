package com.soen342.model;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.soen342.catalog.History;
import com.soen342.model.enums.ActivityType;
import com.soen342.model.enums.Priority;
import com.soen342.model.enums.TaskStatus;
import com.soen342.persistence.DBUtil;

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

    private Project project;
    private RecurrencePattern recurrencePattern;
    private final List<Subtask> subtasks = new ArrayList<>();
    private final List<Tag> tags = new ArrayList<>();
    private final List<ActivityEntry> activityHistory = new ArrayList<>();
    private final List<TaskOccurrence> occurrences = new ArrayList<>();

    public Task(int id, String title, String description, Priority priority) {
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

    private Task(int id, String title, String description, Priority priority, TaskStatus status, boolean isRecurring, LocalDate dueDate, LocalDateTime createdOn) {
        this.taskId = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.isRecurring = isRecurring;
        this.status = status;
        this.dueDate = dueDate;
        this.createdOn = createdOn;
    }

    public void complete() {
        if (StatusCheck(TaskStatus.COMPLETED)) return;
        TaskStatus prev = getStatus();
        this.status = TaskStatus.COMPLETED;
        try {
            DBUtil.editTask(this);
        } catch (SQLException e) {
            this.status = prev;
            throw new RuntimeException("Failed to update task.", e);
        }
        recordActivity(ActivityType.COMPLETED, "Task '" + title + "' completed.");
    }

    public void cancel() {
        if (StatusCheck(TaskStatus.CANCELLED)) return;
        TaskStatus prev = getStatus();
        this.status = TaskStatus.CANCELLED;
        try {
            DBUtil.editTask(this);
        } catch (SQLException e) {
            this.status = prev;
            throw new RuntimeException("Failed to update task.", e);
        }
        recordActivity(ActivityType.CANCELLED, "Task '" + title + "' cancelled.");
    }

    public void reopen() {
        if (StatusCheck(TaskStatus.OPEN)) return;

        TaskStatus prev = this.status;
        this.status = TaskStatus.OPEN;
        try {
            DBUtil.editTask(this);
        } catch (SQLException e) {
            this.status = prev;
            throw new RuntimeException("Failed to update task.", e);
        }
        recordActivity(ActivityType.UPDATED, "Task '" + title + "' reopened.");
    }

    public Subtask addSubtask(String subTitle) {
        if (subtasks.size() >= 20) {
            throw new IllegalStateException("Max 20 subtasks.");
        }
        Subtask subtask = new Subtask(subTitle);
        try {
            DBUtil.saveSubtask(subtask, this.taskId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save subtask.", e);
        }
        subtasks.add(subtask);
        recordActivity(ActivityType.UPDATED, "Subtask added.");
        return subtask;
    }

    public void markSubtaskCancelled(int subtaskId) {
        Subtask subtask = getSubtask(subtaskId);
        subtask.cancel();
        recordActivity(ActivityType.UPDATED, "Subtask cancelled.");
    }

    private void recordActivity(ActivityType type, String description) {
        ActivityEntry entry = new ActivityEntry(type, "task #" + this.taskId + ": " + description);
        history.record(entry);
        activityHistory.add(entry);
    }

    private Subtask getSubtask(int subtaskId) {
        return subtasks.stream()
                .filter(s -> s.getSubtaskId() == subtaskId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Subtask not found"));
    }

    private boolean StatusCheck(TaskStatus status) {
        return this.status == status;
    }
}