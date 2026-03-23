package com.soen342.model;

import com.soen342.model.enums.ActivityType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ActivityEntry {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ActivityType type;
    private final LocalDateTime timeStamp;
    private final String description;

    public ActivityEntry(ActivityType type, String description) {
        this.type = type;
        this.timeStamp = LocalDateTime.now();
        this.description = description;
    }

    // --- Getters ---

    public ActivityType getType() { return type; }

    public LocalDateTime getTimeStamp() { return timeStamp; }

    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "[" + timeStamp.format(FORMATTER) + "] " + type + ": " + description;
    }
}
