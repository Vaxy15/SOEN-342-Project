package com.soen342.catalog;

import com.soen342.model.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages all projects. Project names are unique.
 * Owned by Console (1 instance).
 */
public class ProjCatalog {

    private static int projectIdCounter = 1;
    private final List<Project> projects = new ArrayList<>();

    public Project createProject(String name, String description) {
        if (findByName(name).isPresent()) {
            throw new IllegalArgumentException(
                "A project named '" + name + "' already exists. Project names must be unique.");
        }
        Project project = new Project(projectIdCounter++, name, description);
        projects.add(project);
        return project;
    }

    public Optional<Project> findByName(String name) {
        return projects.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public Optional<Project> findById(int projectId) {
        return projects.stream()
                .filter(p -> p.getProjectId() == projectId)
                .findFirst();
    }

    /**
     * Returns existing project or creates a new one — used during CSV import.
     */
    public Project findOrCreate(String name, String description) {
        return findByName(name).orElseGet(() -> createProject(name, description));
    }

    public void deleteProject(Project project) {
        // Remove all task-project links before deleting
        new ArrayList<>(project.getTasks()).forEach(project::removeTask);
        projects.remove(project);
    }

    public void updateProject(Project project, String newName, String newDescription) {
        if (newName != null && !newName.equalsIgnoreCase(project.getName())) {
            if (findByName(newName).isPresent()) {
                throw new IllegalArgumentException(
                    "A project named '" + newName + "' already exists.");
            }
            project.setName(newName);
        }
        if (newDescription != null) project.setDescription(newDescription);
    }

    public List<Project> getAllProjects() {
        return new ArrayList<>(projects);
    }
}
