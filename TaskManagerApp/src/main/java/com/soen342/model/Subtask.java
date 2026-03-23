package com.soen342.model;

import com.soen342.model.enums.TaskStatus;

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

    // --- Getters & Setters ---

    public int getSubtaskId() { return subtaskId; }

    public String getSubTitle() { return subTitle; }

    public void setSubTitle(String subTitle) { this.subTitle = subTitle; }

    public void complete() { this.subStatus = TaskStatus.COMPLETED; }

    public void cancel() { this.subStatus = TaskStatus.CANCELLED; }

    public void reopen() { this.subStatus = TaskStatus.OPEN; }

    public TaskStatus getSubStatus() { return subStatus; }

    public Collaborator getLinkedCollaborator() { return linkedCollaborator; }

    public void setLinkedCollaborator(Collaborator collaborator) {
        this.linkedCollaborator = collaborator;
    }

    public boolean isLinkedToCollaborator() { return linkedCollaborator != null; }

    @Override
    public String toString() {
        String collabInfo = linkedCollaborator != null
                ? " [Collaborator: " + linkedCollaborator.getName() + "]"
                : "";
        return "  Subtask #" + subtaskId + ": " + subTitle + " [" + subStatus + "]" + collabInfo;
    }
}
