package com.soen342.catalog;

import com.soen342.model.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages all tags. Owned by Console.
 */
public class TagCatalog {

    private final List<Tag> tags = new ArrayList<>();

    public Tag createTag(String name) {
        if (findByName(name).isPresent()) {
            throw new IllegalArgumentException("Tag '" + name + "' already exists.");
        }
        Tag tag = new Tag(name);
        tags.add(tag);
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
