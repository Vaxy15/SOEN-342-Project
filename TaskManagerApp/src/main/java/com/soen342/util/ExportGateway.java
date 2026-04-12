package com.soen342.util;

import com.soen342.model.Project;
import com.soen342.model.Task;
import net.fortuna.ical4j.validate.ValidationException;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;

public interface ExportGateway {
    /**
     *
     * Gateway Pattern specification
     * <p>
     *     exports list of tasks to ics format, given a file location
     * </p>
     *
     * @param tasks
     * @param filename
     * @throws FileAlreadyExistsException if file already exists at given location
     * @throws IOException if an error occurs during file write
     */
    public void exportFilteredICS(List<Task> tasks, String filename) throws FileAlreadyExistsException, IOException;

    /**
     *
     * Gateway Pattern specification
     * <p>
     *     exports a single task to ics format, given a file location
     * </p>
     *
     * @param task
     * @param filename
     * @throws FileAlreadyExistsException if file already exists at given location
     * @throws IOException if an error occurs during file write
     */
    public void exportTaskICS(Task task, String filename) throws FileAlreadyExistsException, IOException;

    /**
     *
     * Gateway Pattern specification
     * <p>
     *     exports all tasks within a project to ics format, given a file location
     * </p>
     *
     * @param project
     * @param filename
     * @throws FileAlreadyExistsException if file already exists at given location
     * @throws IOException if an error occurs during file write
     */
    public void exportProjectICS(Project project, String filename) throws FileAlreadyExistsException, IOException;
}
