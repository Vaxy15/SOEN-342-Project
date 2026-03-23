package com.soen342.model;

import com.soen342.model.enums.RecurrenceType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RecurrencePattern {

    private static int idCounter = 1;

    private final int patternId;
    private RecurrenceType type;
    private int interval;            // e.g., every N days/weeks/months
    private LocalDate startDate;
    private LocalDate endDate;
    private String selectedDays;     // e.g., "MON,WED,FRI" for weekly
    private int dayOfMonth;          // for monthly recurrence

    public RecurrencePattern(RecurrenceType type, int interval,
                             LocalDate startDate, LocalDate endDate) {
        this.patternId = idCounter++;
        this.type = type;
        this.interval = interval;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Generates all occurrence due dates based on the recurrence pattern.
     */
    public List<LocalDate> generateOccurrenceDates() {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            switch (type) {
                case DAILY:
                    dates.add(current);
                    current = current.plusDays(interval);
                    break;

                case WEEKLY:
                    dates.add(current);
                    current = current.plusWeeks(interval);
                    break;

                case MONTHLY:
                    dates.add(current);
                    current = current.plusMonths(interval);
                    break;

                case CUSTOM:
                    dates.add(current);
                    current = current.plusDays(interval);
                    break;

                default:
                    current = endDate.plusDays(1); // exit loop
            }
        }
        return dates;
    }

    // --- Getters & Setters ---

    public int getPatternId() { return patternId; }

    public RecurrenceType getType() { return type; }

    public void setType(RecurrenceType type) { this.type = type; }

    public int getInterval() { return interval; }

    public void setInterval(int interval) { this.interval = interval; }

    public LocalDate getStartDate() { return startDate; }

    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }

    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getSelectedDays() { return selectedDays; }

    public void setSelectedDays(String selectedDays) { this.selectedDays = selectedDays; }

    public int getDayOfMonth() { return dayOfMonth; }

    public void setDayOfMonth(int dayOfMonth) { this.dayOfMonth = dayOfMonth; }

    @Override
    public String toString() {
        return "RecurrencePattern #" + patternId + " [" + type + ", every " + interval
                + ", " + startDate + " to " + endDate + "]";
    }
}
