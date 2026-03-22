package com.soen342.model.enums;

public enum CollaboratorCategory {
    SENIOR(2),
    INTERMEDIATE(5),
    JUNIOR(10);

    private final int maxOpenTasks;

    CollaboratorCategory(int maxOpenTasks) {
        this.maxOpenTasks = maxOpenTasks;
    }

    public int getMaxOpenTasks() {
        return maxOpenTasks;
    }
}
