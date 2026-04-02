package com.soen342.model;

import com.soen342.model.enums.TaskStatus;
import com.soen342.persistence.DBUtil;

import java.sql.SQLException;

public class Subtask {

    private static int idCounter = 1;

    private final int subtaskId;
    private String subTitle;
    private TaskStatus subStatus;

    // Optional link to a collaborator (for collaborated subtasks)
    private Collaborator linkedCollaborator;

    public Subtask(String subTitle) {
        this.subtaskId = idCounter++;
        this.subTitle = subTitle;
        this.subStatus = TaskStatus.OPEN;
    }

    public Subtask(int id, String subTitle, TaskStatus status) {

        this.subtaskId = id;
        this.subTitle = subTitle;
        this.subStatus = status;

        idCounter = Math.max(idCounter, id + 1);
    }

    // --- Getters & Setters ---

    public int getSubtaskId() { return subtaskId; }

    public String getSubTitle() { return subTitle; }

    public void setSubTitle(String subTitle) {
        String old = this.subTitle;
        this.subTitle = subTitle;
        try {
            DBUtil.editSubtask(this);
        } catch (SQLException e) {
            this.subTitle = old;
            throw new RuntimeException("Failed to update subtask, operation aborted.", e);
        }
    }

    public void complete() {
        TaskStatus prev = subStatus;
        this.subStatus = TaskStatus.COMPLETED;
        try {
            DBUtil.editSubtask(this);
        } catch (SQLException e) {
            this.subStatus = prev;
            throw new RuntimeException("Failed to update subtask, operation aborted.", e);
        }
    }

    public void cancel() {
        TaskStatus prev = subStatus;
        this.subStatus = TaskStatus.CANCELLED;
        try {
            DBUtil.editSubtask(this);
        } catch (SQLException e) {
            this.subStatus = prev;
            throw new RuntimeException("Failed to update subtask, operation aborted.", e);
        }
    }

    public void reopen() {
        TaskStatus prev = subStatus;
        this.subStatus = TaskStatus.OPEN;
        try {
            DBUtil.editSubtask(this);
        } catch (SQLException e) {
            this.subStatus = prev;
            throw new RuntimeException("Failed to update subtask, operation aborted.", e);
        }
    }

    public TaskStatus getSubStatus() { return subStatus; }

    public Collaborator getLinkedCollaborator() { return linkedCollaborator; }

    public void setLinkedCollaborator(Collaborator collaborator) {
        this.linkedCollaborator = collaborator;
        try {
            DBUtil.editSubtask(this);
        } catch (SQLException e) {
            this.linkedCollaborator = null;
            throw new RuntimeException("Failed to update subtask, operation aborted.", e);
        }
    }

    public boolean isLinkedToCollaborator() { return linkedCollaborator != null; }

    @Override
    public String toString() {
        String collabInfo = linkedCollaborator != null
                ? " [Collaborator: " + linkedCollaborator.getName() + "]"
                : "";
        return "  Subtask #" + subtaskId + ": " + subTitle + " [" + subStatus + "]" + collabInfo;
    }

    //raw data setting - bypasses business logic
    public void setLinkedCollaboratorRaw(Collaborator collaborator) {
        this.linkedCollaborator = collaborator;
    }
}
