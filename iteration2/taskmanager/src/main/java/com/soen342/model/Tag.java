package com.soen342.model;

public class Tag {

    private static int idCounter = 1;

    private final int tagId;
    private String name;

    public Tag(String name) {
        this.tagId = idCounter++;
        this.name = name;
    }

    // --- Getters & Setters ---

    public int getTagId() { return tagId; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "#" + name + " (id=" + tagId + ")";
    }
}
