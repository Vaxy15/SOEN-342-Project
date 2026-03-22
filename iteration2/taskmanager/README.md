# Personal Task Management System
### SOEN 342 — Winter 2026 | Iteration II

A command-line Java application for managing personal tasks, projects, subtasks, tags, collaborators, and recurring tasks. Built following a formal object-oriented software engineering process (domain model → interaction diagrams → class diagram → implementation).

---

## Requirements

- **Java 17+** (Eclipse Adoptium JDK recommended)
- No external libraries or frameworks required

Verify your setup:
```cmd
java -version
javac -version
```

---

## Getting Started

### 1. Clone / download the project
Place the `taskmanager/` folder somewhere on your machine, e.g.:
```
C:\Users\you\Desktop\taskmanager\
```

### 2. Open a terminal in that folder
```cmd
cd C:\Users\you\Desktop\taskmanager
```

### 3. Create the output directory
```cmd
mkdir out
```

### 4. Compile

**PowerShell:**
```powershell
Get-ChildItem -Recurse -Filter "*.java" src | ForEach-Object { $_.FullName } | Out-File -Encoding utf8 sources.txt
javac -d out "@sources.txt"
```

**Or as a one-liner (no file needed):**
```powershell
javac -d out (Get-ChildItem -Recurse -Filter "*.java" src | ForEach-Object { $_.FullName })
```

**CMD:**
```cmd
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
```

### 5. Run
```cmd
java -cp out com.soen342.Main
```

---

## Project Structure
```
taskmanager/
└── src/main/java/com/soen342/
    │
    ├── Main.java                        ← Entry point, starts the Console
    │
    ├── model/                           ← Domain model (pure data + behaviour)
    │   ├── enums/
    │   │   ├── ActivityType.java        CREATED | UPDATED | COMPLETED | CANCELLED
    │   │   ├── CollaboratorCategory.java SENIOR(2) | INTERMEDIATE(5) | JUNIOR(10)
    │   │   ├── Priority.java            LOW | MEDIUM | HIGH
    │   │   ├── RecurrenceType.java      DAILY | WEEKLY | MONTHLY | CUSTOM
    │   │   └── TaskStatus.java          OPEN | COMPLETED | CANCELLED
    │   │
    │   ├── Task.java                    Core entity — owns subtasks, tags, history
    │   ├── Subtask.java                 Belongs to one parent task only
    │   ├── Project.java                 Groups tasks; has a unique name
    │   ├── Tag.java                     User-defined keyword
    │   ├── Collaborator.java            External contributor with a task cap
    │   ├── ActivityEntry.java           Timestamped log of a task change
    │   ├── RecurrencePattern.java       Defines how a task repeats
    │   └── TaskOccurrence.java          One instance of a recurring task
    │
    ├── catalog/                         ← In-memory "repositories" (one per type)
    │   ├── TaskCatalog.java             Stores all tasks + full search logic
    │   ├── ProjCatalog.java             Stores all projects; enforces unique names
    │   ├── TagCatalog.java              Stores all tags
    │   ├── CollaboratorCatalog.java     Stores all collaborators
    │   └── History.java                 Wraps task activity log access
    │
    ├── util/
    │   └── CsvUtil.java                 Import tasks from CSV / export to CSV
    │
    └── console/
        └── Console.java                 CLI controller — all user interaction
```

---

## How the Code Works

### The Big Picture

The system follows a layered architecture derived directly from the UML diagrams:
```
User input
    ↓
Console  (controller / facade)
    ↓
Catalogs  (in-memory storage + queries)
    ↓
Model classes  (Task, Project, Subtask, etc.)
```

`Console` is the only class the user ever talks to. It owns all the catalogs and routes every action to the right place. Nothing in the model layer talks to the user directly.

---

### Model Layer — `com.soen342.model`

This is where the business rules live.

**`Task`** is the central class. When you create one it immediately logs a `CREATED` activity entry. Every mutation (complete, cancel, add subtask, add tag, set due date) logs another entry automatically. Key rules enforced here:
- Title and priority are mandatory — the constructor throws if either is missing
- Completing all subtasks does **not** auto-complete the parent task (per spec)
- A collaborator can only be assigned if the task belongs to a project and the collaborator is a member of that project

**`Subtask`** cannot exist on its own — it is always created through `task.addSubtask(...)` and lives inside the task's list. If a subtask is linked to a `Collaborator`, that reference is stored on the subtask itself.

**`Collaborator`** enforces capacity limits. When you call `collaborator.assignSubtask(subtask)`, it counts the collaborator's current open subtasks and throws an `IllegalStateException` if the limit for their category is exceeded (Senior=2, Intermediate=5, Junior=10). The check happens automatically regardless of where the assignment is triggered from.

**`RecurrencePattern`** stores the schedule rules (type, interval, start/end dates). Calling `generateOccurrenceDates()` returns a list of `LocalDate` values. When a pattern is attached to a task via `task.setRecurrencePattern(pattern)`, the task immediately generates all its `TaskOccurrence` objects. Each occurrence is independent — completing one has no effect on the others.

**`ActivityEntry`** is immutable — it captures what happened, when, and what type of action it was. It is created internally by `Task` and never modified.

---

### Catalog Layer — `com.soen342.catalog`

Each catalog is an in-memory list with lookup and query methods. There is no database in this iteration.

**`TaskCatalog`** contains a nested `SearchCriteria` builder class that lets you chain filters:
```java
List<Task> results = taskCatalog.search(
    new TaskCatalog.SearchCriteria()
        .status(TaskStatus.OPEN)
        .priority(Priority.HIGH)
        .keyword("homework")
        .orderBy("duedate")
);
```

If no criteria are set, the search defaults to all open tasks sorted by due date ascending — matching the spec exactly.

**`ProjCatalog`** enforces that project names are unique. It also has a `findOrCreate(name, description)` method used during CSV import to avoid duplicates.

**`TagCatalog`** and **`CollaboratorCatalog`** follow the same pattern — both have `findOrCreate()` for use during import.

---

### Utility Layer — `com.soen342.util`

**`CsvUtil`** handles both directions:

- **Export** — iterates a list of tasks and writes one row per subtask (or one row for the task if it has no subtasks). Optional fields are left empty. Files are always named `tasks_export.csv`.
- **Import** — reads each row, looks up or creates the task (matched by title + due date), then looks up or creates the project, collaborator, and tag as needed. Rows with missing required fields are skipped with a warning rather than crashing.

Column order: `TaskName, Description, Subtask, Status, Priority, DueDate, ProjectName, ProjectDescription, Collaborator, CollaboratorCategory`

---

### Console Layer — `com.soen342.console`

`Console` is the top-level controller (the facade from the domain model). It owns one instance of each catalog and handles all input/output, organized into menus:

| Menu | What it covers |
|---|---|
| Tasks | Create, update, complete, cancel, subtasks, tags, project/collaborator assignment, activity history |
| Projects | Create, update, delete, list |
| Tags | Create, list |
| Collaborators | Create, list |
| Search & View | Full filtered search with optional CSV export of results |
| CSV | Bulk import from file / export entire database |

Each menu option calls a private method that handles input, calls the appropriate catalog or model method, and prints the result. All error cases (invalid ID, collaborator at capacity, project not found, etc.) print a clear `[Error]` message rather than crashing.

---

## CSV Format Reference
```
TaskName,Description,Subtask,Status,Priority,DueDate,ProjectName,ProjectDescription,Collaborator,CollaboratorCategory
Write report,Final report,,OPEN,HIGH,2026-04-01,School,Assignments,,
Write report,Final report,Draft outline,OPEN,HIGH,2026-04-01,School,Assignments,Alice,JUNIOR
```

- `DueDate` format: `yyyy-MM-dd`
- `Status`: `OPEN` / `COMPLETED` / `CANCELLED`
- `Priority`: `LOW` / `MEDIUM` / `HIGH`
- `CollaboratorCategory`: `SENIOR` / `INTERMEDIATE` / `JUNIOR`
- Optional columns can be left empty but the comma must still be present

---

## Common Issues

| Problem | Fix |
|---|---|
| `javac: not found` | Make sure `...\jdk-17...\bin` is in your PATH, then open a fresh terminal |
| `invalid flag` on compile | Re-generate `sources.txt` using `Out-File -Encoding utf8` in PowerShell |
| `ClassNotFoundException` on run | Make sure you ran `mkdir out` before compiling and are in the `taskmanager\` directory |
| Export says "directory not found" | Pass a full path like `C:\Users\you\Desktop`, not a filename |
| Collaborator assignment fails | The task must belong to a project and the collaborator must be under that project first |