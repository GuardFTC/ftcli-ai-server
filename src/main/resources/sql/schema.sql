-- 工具规格表
CREATE TABLE IF NOT EXISTS tool_spec
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT NOT NULL UNIQUE,
    description TEXT NOT NULL,
    type        TEXT NOT NULL
);

-- 工具参数表
CREATE TABLE IF NOT EXISTS tool_spec_param
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    tool_spec_id  INTEGER NOT NULL,
    name          TEXT    NOT NULL,
    description   TEXT    NOT NULL,
    required      INTEGER NOT NULL DEFAULT 0,
    type          TEXT    NOT NULL,
    enum_values   TEXT,
    FOREIGN KEY (tool_spec_id) REFERENCES tool_spec (id),
    UNIQUE (tool_spec_id, name)
);
