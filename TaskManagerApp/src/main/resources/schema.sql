
CREATE TABLE IF NOT EXISTS projects (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS collaborators (
    id INTEGER PRIMARY KEY,
    name TEXT,
    category TEXT CHECK(category IN ('SENIOR', 'INTERMEDIATE', 'JUNIOR')),
    project_id INTEGER,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS recurrence_patterns (
    id INTEGER PRIMARY KEY,
    type TEXT CHECK(type IN ('DAILY', 'WEEKLY', 'MONTHLY', 'CUSTOM')),
    interval INTEGER,
    start_date TEXT,
    end_date TEXT,
    selected_days TEXT,
    day_of_month INTEGER
);

CREATE TABLE IF NOT EXISTS tasks (
    id INTEGER PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT,
    priority TEXT CHECK(priority IN ('LOW', 'MEDIUM', 'HIGH')),
    status TEXT CHECK(status IN ('OPEN', 'CANCELLED', 'COMPLETED')),
    due_date TEXT,
    is_recurring BOOLEAN,
    created_at TEXT NOT NULL,
    recurrence_pattern_id INTEGER,
    project_id INTEGER,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL,
    FOREIGN KEY (recurrence_pattern_id) REFERENCES recurrence_patterns(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS subtasks (
    id INTEGER PRIMARY KEY,
    title TEXT NOT NULL,
    status TEXT CHECK(status IN ('OPEN', 'CANCELLED', 'COMPLETED')),
    task_id INTEGER NOT NULL,
    collaborator_id INTEGER,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (collaborator_id) REFERENCES collaborators(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS tags (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS task_tag_combination (
    task_id INTEGER NOT NULL,
    tag_id INTEGER NOT NULL,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (task_id, tag_id)
);

CREATE TABLE IF NOT EXISTS activity_entries (
    id INTEGER PRIMARY KEY,
    type TEXT CHECK(type IN ('CREATED', 'UPDATED', 'COMPLETED', 'CANCELLED')),
    description TEXT NOT NULL,
    timestamp TEXT NOT NULL
);

