package com.soen342.model;

import com.soen342.model.enums.Priority;
import com.soen342.model.enums.TaskStatus;

import java.time.LocalDate;

public class TaskOccurrence {

    private static int idCounter = 1;

    private final int occurrenceId;
    private final Task parentTask;
    private LocalDate dueDate;
    private Priority priority;
    private TaskStatus status;

    public TaskOccurrence(Task parentTask, LocalDate dueDate) {
        this.occurrenceId = idCounter++;
        this.parentTask = parentTask;
        this.dueDate = dueDate;
        this.priority = parentTask.getPriority();
        this.status = TaskStatus.OPEN;
    }

    /**
     * Completing one occurrence does NOT complete the parent task or future occurrences.
     */
    public void complete() {
        this.status = TaskStatus.COMPLETED;
    }

    public void cancel() {
        this.status = TaskStatus.CANCELLED;
    }

    // --- Getters & Setters ---

    public int getOccurrenceId() { return occurrenceId; }

    public Task getParentTask() { return parentTask; }

    public LocalDate getDueDate() { return dueDate; }

    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Priority getPriority() { return priority; }

    public void setPriority(Priority priority) { this.priority = priority; }

    public TaskStatus getStatus() { return status; }

    public void setStatus(TaskStatus status) { this.status = status; }

    /** The unique identifier per spec: task name + due date */
    public String getUniqueKey() {
        return parentTask.getTitle() + "_" + dueDate.toString();
    }

    @Override
    public String toString() {
        return "  Occurrence #" + occurrenceId + " due " + dueDate
                + " [" + status + ", " + priority + "]";
    }
}
