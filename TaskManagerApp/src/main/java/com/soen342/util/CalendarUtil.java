package com.soen342.util;

import com.soen342.model.Subtask;
import com.soen342.model.Task;
import com.soen342.model.TaskOccurrence;
import com.soen342.model.enums.TaskStatus;
import com.soen342.model.enums.Priority;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.validate.ValidationException;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CalendarUtil implements ExportGateway{

    private static List<VToDo> parseVToDoComponentList(Task task){

        List<VToDo> todos = new ArrayList<>();

        if (task.isRecurring()) {
            task.getRecurrencePattern().generateOccurrenceDates();
            for(TaskOccurrence occ : task.getOccurrences())
                todos.add(parseVToDoComponent(occ));
            return todos;
        }
        if (task.getDueDate() == null) return todos;

        Name name = new Name(task.getTitle());
        Description description = parseDescriptionProperty(task);
        Status status = parseStatusProperty(task.getStatus());
        // inline import to avoid confusion between our Priority Object and library's Priority
        net.fortuna.ical4j.model.property.Priority priority = parsePriority(task.getPriority());
        Due<LocalDate> due = new Due<>(task.getDueDate());
        RelatedTo relatedTo = new RelatedTo(task.getProject().getName());

        List<Property> properties = List.of(
                name,
                description,
                status,
                priority,
                due,
                relatedTo
        );
        todos.add(new VToDo(new PropertyList(properties)));
        return todos;
    }


    private static net.fortuna.ical4j.model.property.Priority parsePriority(Priority priority) {
        switch (priority) {

            case LOW -> {return new net.fortuna.ical4j.model.property.Priority(
                    net.fortuna.ical4j.model.property.Priority.VALUE_LOW);}
            case MEDIUM -> { return new net.fortuna.ical4j.model.property.Priority(
                    net.fortuna.ical4j.model.property.Priority.VALUE_MEDIUM
            );}

            default -> {return new net.fortuna.ical4j.model.property.Priority(
                    net.fortuna.ical4j.model.property.Priority.VALUE_HIGH
            );}
        }
    }

    private static Status parseStatusProperty(TaskStatus status) {
        switch (status){

            case COMPLETED -> {return new Status(Status.VALUE_COMPLETED);}
            case CANCELLED -> {return new Status(Status.VALUE_CANCELLED);}
            default -> {return new Status(Status.VALUE_IN_PROCESS);}
        }
    }

    private static Description parseDescriptionProperty(Task task) {
        StringBuilder descriptionSB = new StringBuilder(task.getDescription());
        if (!task.getSubtasks().isEmpty()) descriptionSB.append("\nSubtasks:\n");
        for (Subtask subtask : task.getSubtasks()) {
            descriptionSB.append(subtask.getSubTitle());
            descriptionSB.append("\n");
        }
        return new Description(descriptionSB.toString());
    }

    private static VToDo parseVToDoComponent(TaskOccurrence taskOccurrence){

        Task parent = taskOccurrence.getParentTask();
        Name nameProperty = new Name(parent.getTitle());
        Description descriptionProperty = parseDescriptionProperty(parent);
        Status statusProperty = parseStatusProperty(taskOccurrence.getStatus());
        net.fortuna.ical4j.model.property.Priority priorityProperty = parsePriority(taskOccurrence.getPriority());
        Due<LocalDate> dueProperty = new Due<>(taskOccurrence.getDueDate());
        RelatedTo relatedTo = new RelatedTo(parent.getProject().getName());

        List<Property> properties = List.of(
                nameProperty,
                descriptionProperty,
                statusProperty,
                priorityProperty,
                dueProperty,
                relatedTo
        );
        return new VToDo(new PropertyList(properties));
    }


    // gateway implementation
    public void exportTasksICS(List<Task> tasks, String fileName) throws FileAlreadyExistsException, IOException {
        Path path = Path.of(fileName);
        // this is to verify if location already has an existing file
        Files.createFile(path);

        Calendar calendar = new Calendar();
        for (Task task : tasks) {
            List<VToDo> vtodos = parseVToDoComponentList(task);
            for (VToDo vtodo : vtodos) {
                calendar.add(vtodo);
            }
        }

        CalendarOutputter outputter = new CalendarOutputter();
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            outputter.output(calendar, fos);
        } catch (ValidationException e) {
            throw new RuntimeException("Error validating calendar, parsing doesnt follow RFC 5545", e);
        }
    }
}
