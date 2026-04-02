package com.soen342.model;

import com.soen342.model.enums.CollaboratorCategory;
import com.soen342.model.enums.TaskStatus;
import com.soen342.persistence.DBUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Collaborator {

    private final int collaboratorId;
    private String name;
    private CollaboratorCategory category;

    // All subtasks assigned to this collaborator
    private final List<Subtask> assignedSubtasks = new ArrayList<>();

    public Collaborator(int id, String name, CollaboratorCategory category) {
        this.collaboratorId = id;
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

    public void setName(String name) {
        String old = this.name;
        this.name = name;
        try {
            DBUtil.editCollaborator(this);
        } catch (SQLException e) {
            this.name = old;
            throw new RuntimeException("Failed to update collaborator, operation aborted.", e);
        }
    }

    public CollaboratorCategory getCategory() { return category; }

    public void setCategory(CollaboratorCategory category) {
        CollaboratorCategory old = this.category;
        this.category = category;
        try {
            DBUtil.editCollaborator(this);
        } catch (SQLException e) {
            this.category = old;
            throw new RuntimeException("Failed to update collaborator, operation aborted.", e);
        }
    }

    public List<Subtask> getAssignedSubtasks() { return assignedSubtasks; }

    @Override
    public String toString() {
        return "Collaborator #" + collaboratorId + ": " + name
                + " [" + category + ", open: " + countOpenTasks()
                + "/" + category.getMaxOpenTasks() + "]";
    }

    //raw data setting for db - skips business logic
    public void addSubtaskRaw(Subtask subtask) {
        this.assignedSubtasks.add(subtask);
    }
}
