package com.soen342.model;

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
            tasks.add(task);
            task.setProject(this);
        }
    }

    public void removeTask(Task task) {
        tasks.remove(task);
        task.setProject(null);
    }

    // --- Collaborator Management ---

    public void addCollaborator(Collaborator collaborator) {
        if (!collaborators.contains(collaborator)) {
            collaborators.add(collaborator);
        }
    }

    public void removeCollaborator(Collaborator collaborator) {
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
}
