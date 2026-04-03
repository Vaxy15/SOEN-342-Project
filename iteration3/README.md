# SOEN 342 — Iteration 3
# Personal Task Management System
### SOEN 342 — Winter 2026 | Iteration III

A command-line Java application for managing personal tasks, projects, subtasks, tags, collaborators, recurring tasks, CSV import/export, iCalendar (.ics) export, and persistence. Built following a formal object-oriented software engineering process (requirements → domain model → interaction diagrams → class diagram → implementation → OCL constraints).

---

## Requirements

- **Java 17+**
- **Maven 3.8+** recommended
- Internet is **not required** to run after dependencies are installed

Verify your setup:
```cmd
java -version
javac -version
mvn -version
Getting Started
1. Clone / download the project
Place the project folder somewhere on your machine, for example:
C:\Users\you\Desktop\TaskManagerApp\
2. Open a terminal in that folder
cd C:\Users\you\Desktop\TaskManagerApp
3. Compile the project
If using Maven:
mvn clean compile
4. Run the application
If using Maven:
mvn exec:java -Dexec.mainClass="com.soen342.Main"
If your setup does not support the exec plugin, compile and run manually:
mvn clean package
java -cp target\classes com.soen342.Main
Project Structure
TaskManagerApp/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── com/
                └── soen342/
                    ├── Main.java
                    │
                    ├── model/
                    │   ├── Task.java
                    │   ├── SubTask.java
                    │   ├── Project.java
                    │   ├── Tag.java
                    │   ├── Collaborator.java
                    │   ├── ActivityEntry.java
                    │   ├── RecurrencePattern.java
                    │   ├── TaskOccurrence.java
                    │   └── enums/
                    │       ├── ActivityType.java
                    │       ├── CollaboratorCategory.java
                    │       ├── Priority.java
                    │       ├── RecurrenceType.java
                    │       └── TaskStatus.java
                    │
                    ├── catalog/
                    │   ├── TaskCatalog.java
                    │   ├── ProjCatalog.java
                    │   ├── TagCatalog.java
                    │   ├── CollaboratorCatalog.java
                    │   └── History.java
                    │
                    ├── util/
                    │   ├── CsvUtil.java
                    │   ├── ExportGateway.java
                    │   └── CalendarUtil.java
                    │
                    └── console/
                        └── Console.java

Iteration III Features
This iteration extends the system with the following features:
1. iCalendar (.ics) Export
The system supports exporting task information to .ics format for use in calendar applications such as Google Calendar, Apple Calendar, and Outlook.
Supported export scenarios:
export a single task
export all tasks
export a filtered search result
Important rules:
only tasks with a due date are exported
subtasks are not exported as separate calendar entries
subtasks are included as part of the task description
recurring tasks export their occurrences as separate calendar todo entries
The .ics export is implemented using the Gateway pattern:
ExportGateway defines the export contract
CalendarUtil implements the gateway
iCal4j is used as the external library

2. Overloaded Collaborator Detection
The system can identify overloaded collaborators.
A collaborator is considered overloaded when:
the number of their assigned open subtasks exceeds their taskLimit
This allows the user to:
detect capacity violations
list overloaded collaborators through the console menu

3. Persistency
Iteration III includes the persistency layer so that the system no longer relies only on temporary in-memory state during execution.
The application now supports storing and reloading data between runs, depending on the configured persistence implementation in the project.

4. OCL Constraints
This iteration formalizes important business rules using Object Constraint Language (OCL), including:
a task cannot have more than 20 subtasks
the number of open tasks without a due date cannot exceed 50
each collaborator task limit must be a positive integer
no collaborator may be overloaded
These constraints are documented in the Iteration III deliverables and reflected in the updated UML model.

How the Code Works
High-Level Flow
User input
    ↓
Console
    ↓
Catalogs / Utility Services
    ↓
Model classes
    ↓
External file formats (.csv / .ics) and persistence

The Console class is the main interaction point. It receives user commands and delegates work to the catalogs, model objects, and utility classes.

Main Components
Console Layer
Console handles:
task management menus
project and collaborator menus
search and filtering
CSV import/export
ICS export
overloaded collaborator checks

Relevant Iteration III additions include:
handleICSMenu()
handleExportSingleTask()
handleExportAllTasks()
handleICSExportFromSearch()
ICSExport(List<Task>)

Catalog Layer
The catalogs act as in-memory managers for the domain objects:
TaskCatalog — task storage, filtering, search, lookup by ID
ProjCatalog — project storage and unique project names
TagCatalog — tag storage
CollaboratorCatalog — collaborator storage
History — task activity history access
TaskCatalog.SearchCriteria is used to build advanced searches.

Model Layer
Core domain classes include:
Task — main task entity
SubTask — subtask owned by a task
Project — group of related tasks and collaborators
Collaborator — external collaborator assigned to subtasks
TaskOccurrence — occurrence of a recurring task
RecurrencePattern — recurrence definition
ActivityEntry — task history log

Important business rules in the model include:
a collaborator can only be assigned through project-linked tasks
recurring tasks generate occurrences over time
subtasks belong only to one parent task
activity history is recorded for significant task actions

Utility Layer

CsvUtil
Handles:
import from CSV
export to CSV

ExportGateway
Defines the contract for calendar export.

CalendarUtil
Implements the calendar export logic:
converts tasks into iCalendar todo entries
ignores tasks without due dates
includes subtasks inside the description
uses iCal4j for writing .ics files

Running ICS Export
From the console, the user can:
choose the ICS export menu
export a single task, all tasks, or search results
provide a file path
generate a .ics file
Example output file:
my_tasks.ics

External Dependency
This project uses:
iCal4j for iCalendar generation
Configured through Maven in pom.xml.

Common Issues
Problem	Fix
mvn: command not found	Install Maven and add it to your PATH
ClassNotFoundException	Make sure the project compiled successfully before running
ICS export says file already exists	Provide a new path or remove the old file first
ICS export produces no entries	Make sure the tasks being exported have due dates
Collaborator overload detected	Reduce open assigned subtasks or increase taskLimit if allowed by the model
CSV import/export not working	Check file path and ensure CSV columns match expected format
