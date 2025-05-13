-- ユーザー情報
create table user_account (
    user_id uuid primary key,
    username varchar(64) not null unique,
    display_name varchar(128)
);
comment on table user_account is 'チケットの報告者や担当者となるユーザーの基本情報。';
comment on column user_account.user_id is 'ユーザーのUUID主キー。';
comment on column user_account.username is 'ログイン等で使用される一意なユーザー名。';
comment on column user_account.display_name is '画面表示用のユーザー名（ニックネームなど）。';

-- プロジェクト情報
create table project (
    project_id uuid primary key,
    name varchar(128) not null
);
comment on table project is 'チケットが紐づくプロジェクト情報。';
comment on column project.project_id is 'プロジェクトのUUID主キー。';
comment on column project.name is 'プロジェクト名。';

-- 状態マスタ
create table ticket_status (
    status_code varchar(32) primary key,
    label varchar(64) not null
);
comment on table ticket_status is 'チケットの状態を表すマスタ（例: open, in_progress, closed など）。';
comment on column ticket_status.status_code is '状態コード（主キー）。';
comment on column ticket_status.label is '画面表示用の状態ラベル。';

-- 優先度マスタ
create table ticket_priority (
    priority_code varchar(32) primary key,
    label varchar(64) not null
);
comment on table ticket_priority is 'チケットの優先度（例: high, medium, low）を表すマスタ。';
comment on column ticket_priority.priority_code is '優先度コード（主キー）。';
comment on column ticket_priority.label is '画面表示用の優先度ラベル。';

-- 種別マスタ
create table ticket_type (
    type_code varchar(32) primary key,
    label varchar(64) not null
);
comment on table ticket_type is 'チケットの種別（例: バグ、タスク、要望など）を表すマスタ。';
comment on column ticket_type.type_code is '種別コード（主キー）。';
comment on column ticket_type.label is '画面表示用の種別ラベル。';

-- チケット本体
create table ticket (
    ticket_id uuid primary key,
    subject varchar(256) not null,
    description text,
    status_code varchar(32) not null references ticket_status(status_code),
    priority_code varchar(32) references ticket_priority(priority_code),
    type_code varchar(32) references ticket_type(type_code),
    reporter_id uuid not null references user_account(user_id),
    assignee_id uuid references user_account(user_id),
    project_id uuid references project(project_id),
    created_at timestamp without time zone not null,
    updated_at timestamp without time zone
);
comment on table ticket is 'チケット管理の中心となるエンティティ。問い合わせや作業指示など。';
comment on column ticket.ticket_id is 'チケットのUUIDによる主キー。アプリケーション側で発番。';
comment on column ticket.subject is 'チケットの件名または概要（最大256文字）。';
comment on column ticket.description is 'チケットの詳細な説明。';
comment on column ticket.status_code is '状態コード。ticket_status テーブルを参照。';
comment on column ticket.priority_code is '優先度コード。ticket_priority テーブルを参照。';
comment on column ticket.type_code is '種別コード。ticket_type テーブルを参照。';
comment on column ticket.reporter_id is 'チケットの報告者（user_account.user_id を参照）。';
comment on column ticket.assignee_id is 'チケットの担当者（user_account.user_id を参照、NULL 可）。';
comment on column ticket.project_id is 'チケットが属するプロジェクト（NULL 可）。';
comment on column ticket.created_at is 'チケット作成日時。アプリケーション側で設定。';
comment on column ticket.updated_at is 'チケット更新日時。アプリケーション側で更新。';
