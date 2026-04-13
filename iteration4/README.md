# SOEN 342 — Personal Task Management System
### Iteration 4 · Winter 2026

> A Java CLI application for managing tasks, projects, recurring schedules, collaborators, and data import/export — backed by SQLite persistence.

---

## 🎬 Interactive Demo

The demo below is a scripted, narrated terminal walkthrough of all ten features.
It runs entirely in the browser — no installation required.

**[▶ Launch Interactive Demo](https://github.com/Vaxy15/SOEN-342-Project/iteration4/demo.html)**

> **To set up the link:** enable GitHub Pages on your repository (Settings → Pages → Branch: `main`, folder: `/` or `/docs`), then replace `YOUR_USERNAME` and `YOUR_REPO` above.  
> Alternatively, open `demo.html` locally — just serve it from the `tts-sync/` folder with `node serve.js` and visit `http://localhost:3000`.

The demo covers all ten sections in order, with skip-ahead pills and 1×/2×/3× playback speed:

| # | Section | What it shows |
|---|---------|---------------|
| 1 | App Startup | Silent DB load, banner, main menu |
| 2 | Tasks | Create tag, create task with priority/due date/tag, view task |
| 3 | Recurring Task | Weekly recurrence, 12 auto-generated occurrences, complete one |
| 4 | Projects | Create project, assign task, verify association |
| 5 | Collaborators | Create JUNIOR and SENIOR collaborators, assign, enforce capacity limit |
| 6 | Overloaded Check | OCL constraint confirmed — system blocks overload at assignment time |
| 7 | Search + CSV Export | Multi-filter search, priority sort, export results to CSV |
| 8 | ICS Export | Export project tasks to `.ics` via Gateway + iCal4J |
| 9 | CSV Import | Import 3 tasks, auto-create missing project/collaborator, verify 7 tasks |
| 10 | Exit + Relaunch | Exit gracefully, relaunch, confirm full persistence via search |

---

## 🚀 Running the Application

### Prerequisites
- Java 17+
- Maven 3.8+
- SQLite (bundled via JDBC — no separate install needed)

### Build
```bash
cd TaskManagerApp
mvn clean package -q
```

### Run
```bash
java -jar target/TaskManagerApp-1.0-SNAPSHOT.jar
```

The database file `app.db` is created automatically in the working directory on first launch.

---

## ✨ Features

### Task Management
- Create tasks with title, description, priority (`LOW` / `MEDIUM` / `HIGH`), and optional due date
- Tag tasks with custom labels; filter by tag in search
- View full task details including subtasks, occurrences, and progress

### Recurring Tasks
- Set `DAILY`, `WEEKLY`, `MONTHLY`, or `CUSTOM` recurrence patterns
- Specify interval, start/end dates, and (for weekly) days of the week
- Occurrences are auto-generated and can be completed independently

### Projects
- Group tasks under named projects with descriptions
- Project membership is enforced for collaborator assignment

### Collaborators & Capacity Control (OCL)
- Collaborators have categories: `JUNIOR` (max 10 open tasks), `INTERMEDIATE` (max 5), `SENIOR` (max 2)
- Assigning a collaborator to a task creates a linked subtask automatically
- The system **blocks** assignment when a collaborator is at capacity — preventing overload rather than detecting it after the fact

### Search & Export
- Filter by keyword, status, priority, project, tag, date range, or day of week
- Sort results by due date, priority, status, or title
- Export any search result set directly to CSV

### ICS Export (Gateway Pattern)
- Export single tasks, all tasks, project tasks, or search results to `.ics`
- Backed by the iCal4J library via the `ExportGateway` abstraction
- Compatible with Google Calendar, Outlook, and Apple Calendar

### CSV Import
- Imports tasks using the same column format produced by export
- Auto-creates missing projects and collaborators silently
- Reports `Imported: N, Skipped: M` summary

### Persistence
- All entities (tasks, subtasks, occurrences, projects, collaborators, tags) are persisted to SQLite via `DBManager`
- Data is loaded silently at startup — no visible loading messages
- Full round-trip verified: exit → relaunch → same state

---

## 🏗 Architecture

```
Main.java
└── Console.java          ← CLI loop (start / handleXxxMenu)
    ├── catalog/
    │   ├── TaskCatalog        (create, search, CRUD)
    │   ├── ProjCatalog        (create, findOrCreate)
    │   ├── TagCatalog
    │   ├── CollaboratorCatalog
    │   └── History
    ├── model/
    │   ├── Task               (toString, subtask/occurrence tracking)
    │   ├── Project
    │   ├── Collaborator       (hasCapacity, countOpenTasks)
    │   ├── Subtask
    │   ├── Tag
    │   ├── RecurrencePattern  (generateOccurrences)
    │   └── TaskOccurrence
    ├── persistence/
    │   ├── DBManager          (init, loadIntoCatalogs, close)
    │   └── DBUtil             (save/load helpers)
    └── util/
        ├── CalendarUtil       (iCal4J VTODO builder)
        ├── CsvUtil            (import / export)
        └── ExportGateway      (Gateway pattern over CalendarUtil)
```

**Key design decisions:**
- The **Gateway pattern** decouples the ICS export logic from iCal4J internals
- **OCL capacity constraints** are enforced in `Collaborator.assignSubtask()` at the model layer, not in the UI
- **Static ID counters** (`Task.idCounter`, `Subtask.idCounter`, etc.) are synced from DB on load via `Math.max`

---

## 📁 Folder Structure

```
iteration4/
├── README.md
├── demo.html                        ← standalone interactive demo (serve locally or via Pages)
├── TaskManagerApp/
│   ├── pom.xml
│   ├── app.db                       ← SQLite database (auto-created)
│   └── src/main/java/
│       └── org/example/
│           ├── Main.java
│           ├── console/Console.java
│           ├── catalog/
│           ├── model/
│           ├── persistence/
│           └── util/
└── tts-sync/                        ← demo tooling (not part of submission)
    ├── demo.html
    ├── generateAudio.js
    ├── serve.js
    └── audio/
        └── nar1.mp3 … nar10.mp3
```

---

## 📋 Iteration 4 Checklist

- [x] Task creation with priority, due date, and tags
- [x] Recurring tasks (DAILY / WEEKLY / MONTHLY / CUSTOM) with per-occurrence completion
- [x] Project grouping and task assignment
- [x] Collaborators with category-based capacity limits (OCL constraint enforced)
- [x] Overloaded collaborator detection (reports empty — constraint prevents it)
- [x] Multi-filter task search with sort and CSV export
- [x] ICS export via Gateway pattern (single / all / project / search)
- [x] CSV import with silent auto-creation of missing entities
- [x] SQLite persistence — full round-trip verified on relaunch

---

*SOEN 342 — Software Requirements and Specifications · Concordia University · Winter 2026*
