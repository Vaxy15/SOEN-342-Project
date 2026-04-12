package com.soen342.catalog;

import com.soen342.model.Tag;
import com.soen342.persistence.DBUtil;
import com.soen342.persistence.TDG.TagTDG;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages all tags. Owned by Console.
 */
public class TagCatalog {

    private static int tagIdCounter = 1;
    private final List<Tag> tags = new ArrayList<>();

    public Tag createTag(String name) {
        if (findByName(name).isPresent()) {
            throw new IllegalArgumentException("Tag '" + name + "' already exists.");
        }
        Tag tag = new Tag(tagIdCounter++, name);
        try {
            TagTDG.save(tag);
        } catch (SQLException e) {
            tagIdCounter--;
            throw new RuntimeException("Failed to save tag, operation aborted.", e);
        }
        tags.add(tag);
        return tag;
    }

    //raw data loading - bypasses business logic
    public Tag loadTagFromDataStore(int id, String name) {
        if (findByName(name).isPresent()) {
            throw new IllegalArgumentException("Tag '" + name + "' already exists.");
        }
        if (findById(id).isPresent()) {
            throw new RuntimeException("Tag with id " + id + " already exists.");
        }
        Tag tag = new Tag(id, name);
        tags.add(tag);
        tagIdCounter = Math.max(tagIdCounter, id + 1);
        return tag;
    }

    public Optional<Tag> findByName(String name) {
        return tags.stream()
                .filter(t -> t.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public Optional<Tag> findById(int tagId) {
        return tags.stream()
                .filter(t -> t.getTagId() == tagId)
                .findFirst();
    }

    public Tag findOrCreate(String name) {
        return findByName(name).orElseGet(() -> createTag(name));
    }

    public List<Tag> getAllTags() {
        return new ArrayList<>(tags);
    }
}
