package com.soen342.model;

public class Tag {

    private final int tagId;
    private final String name;

    public Tag(int id, String name) {
        this.tagId = id;
        this.name = name;
    }

    // --- Getters & Setters ---

    public int getTagId() { return tagId; }

    public String getName() { return name; }

    @Override
    public String toString() {
        return "#" + name + " (id=" + tagId + ")";
    }
}
