package com.soen342.catalog;

import com.soen342.model.ActivityEntry;
import com.soen342.model.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds all activity entries across all tasks.
 * Owned by Console (1 instance).
 */
public class History {

    private final List<ActivityEntry> allEntries = new ArrayList<>();

    public void record(ActivityEntry entry) {
        allEntries.add(entry);
    }

    /**
     * Returns the full activity history for a specific task.
     */
    public List<ActivityEntry> getHistoryForTask(Task task) {
        return task.getActivityHistory();
    }

    public List<ActivityEntry> getAllEntries() {
        return allEntries;
    }

    public void printTaskHistory(Task task) {
        List<ActivityEntry> history = task.getActivityHistory();
        if (history.isEmpty()) {
            System.out.println("No activity history for task: " + task.getTitle());
            return;
        }
        System.out.println("--- Activity History: " + task.getTitle() + " ---");
        history.forEach(System.out::println);
    }
}
