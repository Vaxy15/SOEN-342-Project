package com.soen342.catalog;

import com.soen342.model.Project;
import com.soen342.model.Tag;
import com.soen342.model.Task;
import com.soen342.model.enums.Priority;
import com.soen342.model.enums.TaskStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages all tasks in the system. Owned by Console (1 instance).
 */
public class TaskCatalog {

    private final List<Task> tasks = new ArrayList<>();

    // --- CRUD ---

    public void addTask(Task task) {
        tasks.add(task);
    }

    public Optional<Task> findById(int taskId) {
        return tasks.stream().filter(t -> t.getTaskId() == taskId).findFirst();
    }

    public Optional<Task> findByTitleAndDueDate(String title, LocalDate dueDate) {
        return tasks.stream()
                .filter(t -> t.getTitle().equalsIgnoreCase(title)
                        && dueDate.equals(t.getDueDate()))
                .findFirst();
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    // --- Default View: Open tasks sorted by due date ascending ---

    public List<Task> getOpenTasksSortedByDueDate() {
        return tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.OPEN)
                .sorted(Comparator.comparing(
                        t -> t.getDueDate() != null ? t.getDueDate() : LocalDate.MAX))
                .collect(Collectors.toList());
    }

    // --- Search / Filter ---

    public List<Task> searchByKeyword(String keyword) {
        String kw = keyword.toLowerCase();
        return tasks.stream()
                .filter(t -> t.getTitle().toLowerCase().contains(kw)
                        || (t.getDescription() != null
                            && t.getDescription().toLowerCase().contains(kw)))
                .collect(Collectors.toList());
    }

    public List<Task> filterByStatus(TaskStatus status) {
        return tasks.stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Task> filterByPriority(Priority priority) {
        return tasks.stream()
                .filter(t -> t.getPriority() == priority)
                .collect(Collectors.toList());
    }

    public List<Task> filterByProject(Project project) {
        return tasks.stream()
                .filter(t -> project.equals(t.getProject()))
                .collect(Collectors.toList());
    }

    public List<Task> filterByTag(Tag tag) {
        return tasks.stream()
                .filter(t -> t.getTags().contains(tag))
                .collect(Collectors.toList());
    }

    public List<Task> filterByDueDate(LocalDate date) {
        return tasks.stream()
                .filter(t -> date.equals(t.getDueDate()))
                .collect(Collectors.toList());
    }

    public List<Task> filterByDueDateRange(LocalDate from, LocalDate to) {
        return tasks.stream()
                .filter(t -> t.getDueDate() != null
                        && !t.getDueDate().isBefore(from)
                        && !t.getDueDate().isAfter(to))
                .collect(Collectors.toList());
    }

    public List<Task> filterByDayOfWeek(DayOfWeek dayOfWeek) {
        return tasks.stream()
                .filter(t -> t.getDueDate() != null
                        && t.getDueDate().getDayOfWeek() == dayOfWeek)
                .collect(Collectors.toList());
    }

    // --- Combined Search (SearchCriteria builder) ---

    public List<Task> search(SearchCriteria criteria) {
        List<Task> result = new ArrayList<>(tasks);

        if (criteria.getStatus() != null)
            result = result.stream()
                    .filter(t -> t.getStatus() == criteria.getStatus())
                    .collect(Collectors.toList());

        if (criteria.getPriority() != null)
            result = result.stream()
                    .filter(t -> t.getPriority() == criteria.getPriority())
                    .collect(Collectors.toList());

        if (criteria.getProject() != null)
            result = result.stream()
                    .filter(t -> criteria.getProject().equals(t.getProject()))
                    .collect(Collectors.toList());

        if (criteria.getTag() != null)
            result = result.stream()
                    .filter(t -> t.getTags().contains(criteria.getTag()))
                    .collect(Collectors.toList());

        if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
            String kw = criteria.getKeyword().toLowerCase();
            result = result.stream()
                    .filter(t -> t.getTitle().toLowerCase().contains(kw)
                            || (t.getDescription() != null
                                && t.getDescription().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }

        if (criteria.getFromDate() != null && criteria.getToDate() != null)
            result = result.stream()
                    .filter(t -> t.getDueDate() != null
                            && !t.getDueDate().isBefore(criteria.getFromDate())
                            && !t.getDueDate().isAfter(criteria.getToDate()))
                    .collect(Collectors.toList());

        if (criteria.getDayOfWeek() != null)
            result = result.stream()
                    .filter(t -> t.getDueDate() != null
                            && t.getDueDate().getDayOfWeek() == criteria.getDayOfWeek())
                    .collect(Collectors.toList());

        // Default sort: due date ascending
        result.sort(Comparator.comparing(
                t -> t.getDueDate() != null ? t.getDueDate() : LocalDate.MAX));

        // Override sort if specified
        if (criteria.getOrderBy() != null) {
            switch (criteria.getOrderBy()) {
                case "priority" -> result.sort(
                        Comparator.comparing(t -> t.getPriority().ordinal()));
                case "status" -> result.sort(
                        Comparator.comparing(t -> t.getStatus().ordinal()));
                case "title" -> result.sort(
                        Comparator.comparing(Task::getTitle));
            }
        }

        return result;
    }

    // --- Inner SearchCriteria class ---

    public static class SearchCriteria {
        private TaskStatus status;
        private Priority priority;
        private Project project;
        private Tag tag;
        private String keyword;
        private LocalDate fromDate;
        private LocalDate toDate;
        private DayOfWeek dayOfWeek;
        private String orderBy;

        public SearchCriteria status(TaskStatus s)    { this.status = s; return this; }
        public SearchCriteria priority(Priority p)    { this.priority = p; return this; }
        public SearchCriteria project(Project pr)     { this.project = pr; return this; }
        public SearchCriteria tag(Tag t)              { this.tag = t; return this; }
        public SearchCriteria keyword(String kw)      { this.keyword = kw; return this; }
        public SearchCriteria dateRange(LocalDate f, LocalDate t) {
            this.fromDate = f; this.toDate = t; return this;
        }
        public SearchCriteria dayOfWeek(DayOfWeek d) { this.dayOfWeek = d; return this; }
        public SearchCriteria orderBy(String ob)     { this.orderBy = ob; return this; }

        public TaskStatus getStatus()    { return status; }
        public Priority getPriority()    { return priority; }
        public Project getProject()      { return project; }
        public Tag getTag()              { return tag; }
        public String getKeyword()       { return keyword; }
        public LocalDate getFromDate()   { return fromDate; }
        public LocalDate getToDate()     { return toDate; }
        public DayOfWeek getDayOfWeek()  { return dayOfWeek; }
        public String getOrderBy()       { return orderBy; }
    }
}
