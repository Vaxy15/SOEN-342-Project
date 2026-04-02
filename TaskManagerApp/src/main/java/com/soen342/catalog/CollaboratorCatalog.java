package com.soen342.catalog;

import com.soen342.model.Collaborator;
import com.soen342.model.enums.CollaboratorCategory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.soen342.persistence.DBUtil;

/**
 * Manages all collaborators. Owned by Console.
 */
public class CollaboratorCatalog {

    private static int collaboratorIdCounter = 1;
    private final List<Collaborator> collaborators = new ArrayList<>();

    public Collaborator createCollaborator(String name, CollaboratorCategory category) {
        Collaborator c = new Collaborator(collaboratorIdCounter++, name, category);
        try {
            DBUtil.saveCollaborator(c);
        } catch (SQLException e) {
            collaboratorIdCounter--;
            throw new RuntimeException("Failed to save collaborator, operation aborted.", e);
        }
        collaborators.add(c);
        return c;
    }

    public Collaborator loadCollaboratorFromDataStore(int id, String name, CollaboratorCategory category) {
        Collaborator c = new Collaborator(id, name, category);
        collaborators.add(c);
        collaboratorIdCounter = Math.max(collaboratorIdCounter, id + 1);
        return c;
    }

    public Optional<Collaborator> findById(int id) {
        return collaborators.stream()
                .filter(c -> c.getCollaboratorId() == id)
                .findFirst();
    }

    public Optional<Collaborator> findByName(String name) {
        return collaborators.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public Collaborator findOrCreate(String name, CollaboratorCategory category) {
        return findByName(name).orElseGet(() -> createCollaborator(name, category));
    }

    public List<Collaborator> getAllCollaborators() {
        return new ArrayList<>(collaborators);
    }
}
