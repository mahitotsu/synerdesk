CREATE TABLE todo_items (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    due_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE todo_items IS 'ToDoアプリケーションのタスクを格納するテーブル';
COMMENT ON COLUMN todo_items.id IS '一意のID（自動採番）';
COMMENT ON COLUMN todo_items.title IS 'タスクのタイトル（必須）';
COMMENT ON COLUMN todo_items.description IS 'タスクの詳細説明（任意）';
COMMENT ON COLUMN todo_items.is_completed IS 'タスクの完了フラグ。TRUEで完了、FALSEで未完了';
COMMENT ON COLUMN todo_items.due_date IS 'タスクの期限日（任意）';
COMMENT ON COLUMN todo_items.created_at IS 'レコードの作成日時';
COMMENT ON COLUMN todo_items.updated_at IS 'レコードの最終更新日時';