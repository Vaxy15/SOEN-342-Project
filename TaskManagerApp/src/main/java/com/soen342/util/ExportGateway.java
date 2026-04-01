package com.soen342.util;

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
     *     exports list of files to ics format, given a file location
     * </p>
     *
     * @param tasks
     * @param filename
     * @throws FileAlreadyExistsException if file already exists at given location
     * @throws IOException if an error occurs during file write
     */
    public void exportTasksICS(List<Task> tasks, String filename) throws FileAlreadyExistsException, IOException;
}
