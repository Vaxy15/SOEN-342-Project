package com.soen342.model;

import com.soen342.model.enums.CollaboratorCategory;
import com.soen342.model.enums.TaskStatus;

import java.util.ArrayList;
import java.util.List;

public class Collaborator {

    private static int idCounter = 1;

    private final int collaboratorId;
    private String name;
    private CollaboratorCategory category;

    // All subtasks assigned to this collaborator
    private final List<Subtask> assignedSubtasks = new ArrayList<>();

    public Collaborator(String name, CollaboratorCategory category) {
        this.collaboratorId = idCounter++;
        this.name = name;
        this.category = category;
    }

    // --- Capacity Validation ---

    /**
     * Returns how many open subtasks this collaborator currently has.
     */
    public long countOpenTasks() {
        return assignedSubtasks.stream()
                .filter(s -> s.getSubStatus() == TaskStatus.OPEN)
                .count();
    }

    /**
     * Returns true if this collaborator can accept one more task.
     */
    public boolean hasCapacity() {
        return countOpenTasks() < category.getMaxOpenTasks();
    }

    /**
     * Assigns a subtask to this collaborator. Throws if at capacity.
     */
    public void assignSubtask(Subtask subtask) {
        if (!hasCapacity()) {
            throw new IllegalStateException(
                "Collaborator '" + name + "' (" + category + ") has reached the limit of "
                + category.getMaxOpenTasks() + " open tasks."
            );
        }
        subtask.setLinkedCollaborator(this);
        assignedSubtasks.add(subtask);
    }

    public void removeSubtask(Subtask subtask) {
        assignedSubtasks.remove(subtask);
    }

    // --- Getters & Setters ---

    public int getCollaboratorId() { return collaboratorId; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public CollaboratorCategory getCategory() { return category; }

    public void setCategory(CollaboratorCategory category) { this.category = category; }

    public List<Subtask> getAssignedSubtasks() { return assignedSubtasks; }

    @Override
    public String toString() {
        return "Collaborator #" + collaboratorId + ": " + name
                + " [" + category + ", open: " + countOpenTasks()
                + "/" + category.getMaxOpenTasks() + "]";
    }
}
