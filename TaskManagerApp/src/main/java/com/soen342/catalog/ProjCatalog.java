package com.soen342.catalog;

import com.soen342.model.Project;
import com.soen342.persistence.DBUtil;

import java.sql.SQLException;
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

        try {
            DBUtil.saveProject(project);
        } catch (SQLException e) {
            projectIdCounter--;
            throw new RuntimeException("Failed to save project, operation aborted.", e);
        }
        projects.add(project);
        return project;
    }

    public void loadProjFromDataStore(int id, String name, String description) {
        if (findByName(name).isPresent()) {
            throw new IllegalArgumentException(
                    "A project named '" + name + "' already exists. Project names must be unique.");
        }
        if (findById(id).isPresent()) {
            throw new RuntimeException("Project with id " + id + " already exists.");
        }
        Project project = new Project(id, name, description);
        projects.add(project);
        projectIdCounter = Math.max(projectIdCounter, id + 1);
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
        try {
            DBUtil.deleteProject(project.getProjectId());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete project, operation aborted.", e);
        }
        new ArrayList<>(project.getTasks()).forEach(project::removeTask);
        projects.remove(project);
    }

    public void updateProject(Project project, String newName, String newDescription) {
        String oldName = project.getName();
        String oldDescription = project.getDescription();
        if (newName != null && !newName.equalsIgnoreCase(project.getName())) {
            if (findByName(newName).isPresent()) {
                throw new IllegalArgumentException(
                    "A project named '" + newName + "' already exists.");
            }
            project.setName(newName);
        }
        if (newDescription != null) project.setDescription(newDescription);
        try {
            DBUtil.editProject(project);
        } catch (SQLException e) {
            project.setName(oldName);
            project.setDescription(oldDescription);
            throw new RuntimeException("Failed to update project, operation aborted.", e);
        }
    }

    public List<Project> getAllProjects() {
        return new ArrayList<>(projects);
    }
}
