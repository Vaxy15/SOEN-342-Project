package com.soen342.model;

import com.soen342.persistence.DBUtil;
import com.soen342.persistence.TDG.CollaboratorTDG;
import com.soen342.persistence.TDG.TaskTDG;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Project {

    private final int projectId;
    private String name;           // must be unique
    private String description;

    private final List<Task> tasks = new ArrayList<>();
    private final List<Collaborator> collaborators = new ArrayList<>();

    public Project(int id, String name, String description) {
        this.projectId = id;
        this.name = name;
        this.description = description;
    }

    // --- Task Management ---

    public void addTask(Task task) {
        if (!tasks.contains(task)) {
            try {
                TaskTDG.update(task);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to link task to project, operation aborted.", e);
            }
            tasks.add(task);
            task.setProject(this);
        }
    }

    public void removeTask(Task task) {
        task.setProject(null);
        try {
            TaskTDG.update(task);
        } catch (SQLException e) {
            task.setProject(this);
            throw new RuntimeException("Failed to unlink task from project, operation aborted.", e);
        }
        tasks.remove(task);
    }

    // --- Collaborator Management ---

    public void addCollaborator(Collaborator collaborator) {
        if (!collaborators.contains(collaborator)) {
            try {
                CollaboratorTDG.linkToProject(collaborator.getCollaboratorId(), projectId);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to link collaborator to project, operation aborted.", e);
            }
            collaborators.add(collaborator);
        }
    }

    public void removeCollaborator(Collaborator collaborator) {
        try {
            CollaboratorTDG.unlinkFromProject(collaborator.getCollaboratorId());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to unlink collaborator from project, operation aborted.", e);
        }
        collaborators.remove(collaborator);
    }

    public boolean hasCollaborator(Collaborator collaborator) {
        return collaborators.contains(collaborator);
    }

    // --- Getters & Setters ---

    public int getProjectId() { return projectId; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public List<Task> getTasks() { return tasks; }

    public List<Collaborator> getCollaborators() { return collaborators; }

    @Override
    public String toString() {
        return "Project #" + projectId + ": " + name
                + (description != null && !description.isEmpty() ? " - " + description : "")
                + " (" + tasks.size() + " tasks)";
    }

    //raw data setting for db - skips business logic
    public void addTaskRaw(Task task) {
        this.tasks.add(task);
    }
     public void addCollaboratorRaw(Collaborator collaborator) {
        this.collaborators.add(collaborator);
     }
}
