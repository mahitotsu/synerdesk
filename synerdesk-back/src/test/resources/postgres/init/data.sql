INSERT INTO user_account (user_id, username, display_name) VALUES
(gen_random_uuid(), 'alice@example.com', 'Alice Yamamoto'),
(gen_random_uuid(), 'bob@example.com', 'Bob Tanaka'),
(gen_random_uuid(), 'carol@example.com', 'Carol Suzuki'),
(gen_random_uuid(), 'dave@example.com', 'Dave Kobayashi'),
(gen_random_uuid(), 'eve@example.com', 'Eve Sato');

INSERT INTO project (project_id, name) VALUES
(gen_random_uuid(), 'Customer Support System'),
(gen_random_uuid(), 'Internal IT Management'),
(gen_random_uuid(), 'New Product Launch');

INSERT INTO ticket_status (status_code, label) VALUES
('open', 'Open'),
('in_progress', 'In Progress'),
('resolved', 'Resolved'),
('closed', 'Closed');

INSERT INTO ticket_priority (priority_code, label) VALUES
('low', 'Low'),
('medium', 'Medium'),
('high', 'High');

INSERT INTO ticket_type (type_code, label) VALUES
('bug', 'Bug'),
('task', 'Task'),
('feature_request', 'Feature Request'),
('improvement', 'Improvement');