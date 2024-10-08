INSERT INTO `campus`
(`status`, `campus_id`, `campus_name`)
VALUES
(true, 1, 'Ho Chi Minh'),
(true, 2, 'Ha Noi'),
(true, 3, 'Can Tho');

INSERT INTO `permission_category`
(`status`, `permission_category_id`, `permission_category_name`)
VALUES
(true, 1, 'MANAGE_ACCOUNT'),
(true, 2, 'MANAGE_ROLE'),
(true, 3, 'MANAGE_PERMISSION'),
(true, 4, 'MANAGE_CAMPUS'),
(true, 5, 'MANAGE_SUBJECT'),
(true, 6, 'MANAGE_DEPARTMENT'),
(true, 7, 'MANAGE_EXAM'),
(true, 8, 'MANAGE_SCORE');

INSERT INTO `permission`
(`action`, `permission_id`, `permission_name`, `permission_category_id`, `status`)
VALUES
('VIEW_ACCOUNT', 1, 'View account', 1, 1),
('VIEW_ROLE', 2, 'View role', 2, 1),
('VIEW_PERMISSION', 3, 'View permission', 3, 1),
('VIEW_CAMPUS', 4, 'View campus', 4, 1),
('VIEW_SUBJECT', 5, 'View subject', 5, 1),
('VIEW_DEPARTMENT', 6, 'View department', 6, 1),
('VIEW_EXAM', 7, 'View exam', 7, 1),
('VIEW_SCORE', 8, 'View score', 8, 1),

('CREATE_ACCOUNT', 9, 'Create account', 1, 1),
('CREATE_ROLE', 10, 'Create role', 2, 1),
('CREATE_PERMISSION', 11, 'Create permission', 3, 1),
('CREATE_CAMPUS', 12, 'Create campus', 4, 1),
('CREATE_SUBJECT', 13, 'Create subject', 5, 1),
('CREATE_DEPARTMENT', 14, 'Create department', 6, 1),
('CREATE_EXAM', 15, 'Create exam', 7, 1),

('UPDATE_ACCOUNT', 16, 'Update account', 1, 1),
('UPDATE_ROLE', 17, 'Update role', 2, 1),
('UPDATE_PERMISSION', 18, 'Update permission', 3, 1),
('UPDATE_CAMPUS', 19, 'Update campus', 4, 1),
('UPDATE_SUBJECT', 20, 'Update subject', 5, 1),
('UPDATE_DEPARTMENT', 21, 'Update department', 6, 1),
('UPDATE_EXAM', 22, 'Update exam', 7, 1),

('DELETE_ACCOUNT', 23, 'Delete account', 1, 1),
('DELETE_ROLE', 24, 'Delete role', 2, 1),
('DELETE_PERMISSION', 25, 'Delete permission', 3, 1),
('DELETE_CAMPUS', 26, 'Delete campus', 4, 1),
('DELETE_SUBJECT', 27, 'Delete subject', 5, 1),
('DELETE_DEPARTMENT', 28, 'Delete department', 6, 1),
('DELETE_EXAM', 29, 'Delete exam', 7, 1);

INSERT INTO `role`
(`status`, `role_id`, `role_name`, `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`, `deleted_by`)
VALUES
(true, 1, 'ADMIN', '2024-09-30 00:00:00', 1, null, null, null, null),
(true, 2, 'EXAMINER', '2024-09-30 00:00:00', 1, null, null, null, null),
(true, 3, 'HEAD_OF_DEPARTMENT', '2024-09-30 00:00:00', 1, null, null, null, null),
(true, 4, 'LECTURER', '2024-09-30 00:00:00', 1, null, null, null, null);

INSERT INTO `role_permission`
(`status`, `role_id`, `permission_id`)
VALUES
(true, 1, 1),(true, 1, 2),(true, 1, 3),(true, 1, 4),(true, 1, 5),(true, 1, 6),(true, 1, 7),(true, 1, 8),(true, 1, 9),(true, 1, 10),(true, 1, 11),(true, 1, 12),(true, 1, 13),
(true, 1, 14),(true, 1, 15),(true, 1, 16),(true, 1, 17),(true, 1, 18),(true, 1, 19),(true, 1, 20),(true, 1, 21),(true, 1, 22),(true, 1, 23),(true, 1, 24),(true, 1, 25),(true, 1, 26),
(true, 1, 27),(true, 1, 28),(true, 1, 29),
(true, 2, 1),(true, 2, 4),(true, 2, 5),(true, 2, 6),(true, 2, 7),(true, 2, 8),(true, 2, 9),(true, 2, 12),(true, 2, 13),(true, 2, 14),(true, 2, 15),(true, 2, 16),(true, 2, 19),(true, 2, 20),
(true, 2, 21),(true, 2, 22),(true, 2, 26),(true, 2, 27),(true, 2, 28),(true, 2, 29),
(true, 3, 1),(true, 3, 4),(true, 3, 5),(true, 3, 6),(true, 3, 7),(true, 3, 8),(true, 3, 9),(true, 3, 12);

INSERT INTO `account`
(`status`, `account_id`, `name`, `email`, `campus_id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`, `deleted_by`)
VALUES
(true, 1, 'Admin', 'tuyenvtse160607@fpt.edu.vn', 1, '2024-09-30 00:00:00', 1, null, null, null, null),
(true, 5, 'Admin', 'truonghnse160585@fpt.edu.vn', 1, '2024-09-30 00:00:00', 1, null, null, null, null),
(true, 6, 'Admin', 'vuongvtse160599@fpt.edu.vn', 1, '2024-09-30 00:00:00', 1, null, null, null, null),
(true, 2, 'Examiner', 'thanhtuyen66202@gmail.com', 1, '2024-09-30 00:00:00', 1, null, null, null, null),
(true, 3, 'Head of Department', 'thanhtuyen662002@gmail.com', 1, '2024-09-30 00:00:00', 1, null, null, null, null),
(true, 4, 'Lecturer', 'oscarsjoyfuljourney@gmail.com', 1, '2024-09-30 00:00:00', 1, null, null, null, null);

INSERT INTO `account_role`
(`status`, `account_id`, `role_id`)
VALUES
(true, 1, 1),
(true, 1, 2),
(true, 2, 2),
(true, 3, 3),
(true, 4, 4),
(true, 5, 1),
(true, 6, 1);

INSERT INTO `examiner`
(`status`, `account_id`)
VALUES
(true, 1),
(true, 2),
(true, 3);

INSERT INTO `department` 
(`department_name`, `dev_language`, `status`) 
VALUES 
('java coding', 'Java', true),
('java coding', 'Java', true),
('c# coding', 'C#', true),
('c# coding', 'C#', true),
('c# coding', 'C#', true);


INSERT INTO `subject` 
(`subject_name`, `subject_code`, `status`, `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`, `deleted_by`, `department_id`) 
VALUES 
('c# coding', 'PRN231', true, '2023-09-30 09:00:00', 1, null, null, null, null, 1),
('c# coding', 'PRN231', true, '2023-09-30 09:00:00', 1, null, null, null, null, 2),
('java coding', 'JAVA241', true, '2023-09-30 09:00:00', 1, null, null, null, null, 3),
('java coding', 'JAVA241', true, '2023-09-30 09:00:00', 1, null, null, null, null, 4);


INSERT INTO `exam` 
(`exam_code`, `exam_at`, `grading_at`, `publish_at`, `semester_name`, `status`, `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`, `deleted_by`, `subject_id`, `campus_id`) 
VALUES 
('SU23', '2024-10-01 10:00:00', '2024-10-02 15:00:00', '2024-10-03 12:00:00', 'Fall 2024', true, '2024-09-30 09:00:00', 1, null, 1, null, 1, 1, 1),
('SU24', '2024-11-01 10:00:00', '2024-11-02 15:00:00', '2024-11-03 12:00:00', 'Fall 2024', true, '2024-09-30 09:00:00', 2, null, 2, null, 2, 2, 2),
('SU25', '2024-12-01 10:00:00', '2024-12-02 15:00:00', '2024-12-03 12:00:00', 'Fall 2024', true, '2024-09-30 09:00:00', 3, null, 3, null, 3, 3, 3);

INSERT INTO `source` 
(`origin_source_path`, `import_time`) 
VALUES 
('https://example.com/source1', '2024-09-30 10:00:00'),
('https://example.com/source2', '2024-09-30 11:00:00'),
('https://example.com/source3', '2024-09-30 12:00:00'),
('https://example.com/source4', '2024-09-30 13:00:00');

INSERT INTO `exam_database` 
(`data_script`, `status`, `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`, `deleted_by`, `account_id`) 
VALUES 
('CREATE TABLE example_table (id INT, name VARCHAR(100));', true, '2024-09-30 10:00:00', 1, '2024-09-30 10:00:00', 1, NULL, 1, 1),
('CREATE TABLE another_table (id INT, description TEXT);', true, '2024-09-30 10:05:00', 1, '2024-09-30 10:00:00', 1, NULL, 1, 2),
('CREATE TABLE sample_table (id INT, value FLOAT);', true, '2024-09-30 10:10:00', 1, '2024-09-30 10:00:00', 1, NULL, 1, 3);

INSERT INTO `exam_paper` 
(`exam_paper_code`, `status`, `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`, `deleted_by`, `account_id`, `exam_id`, `exam_database_id`, `source_id`) 
VALUES 
('EP001', true, '2024-09-30 10:00:00', 1, NULL, 1, NULL, 1, 1, 1, 1, 1),
('EP002', true, '2024-09-30 10:00:00', 1, NULL, 1, NULL, 2, 2, 2, 2, 2),
('EP003', true, '2024-09-30 10:00:00', 1, NULL, 1, NULL, 3, 3, 3, 3, 3);

INSERT INTO `exam_question` 
(`question_content`, `question_number`, `max_score`, `type`, `status`, `created_at`, `created_by`, `updated_at`, `updated_by`, `account_id`, `exam_paper_id`, `deleted_by`) 
VALUES 
('What is the capital of France?', 'Q1', 5.0, 'Multiple Choice', true, '2023-09-30 10:00:00', 1, '2023-09-30 10:00:00', 1, 1, 1, 1),
('Explain Newton\'s first law of motion.', 'Q2', 10.0, 'Essay', true, '2023-09-30 10:00:00', 2, '2023-09-30 10:00:00', 1, 2, 2, 1),
('Solve the equation: 2x + 3 = 7.', 'Q3', 5.0, 'Short Answer', true, '2023-09-30 10:00:00', 3, '2023-09-30 10:00:00', 1, 3, 3, 1);

INSERT INTO ai_prompt (content, language_code, for_ai, type, status, parent)
VALUES 
    ('Defaut.', 'en', 'GPT-3', 'text', true, NULL),
    ('Explain the laws of thermodynamics in simple terms.', 'en', 'GPT-3', 'text', true, 1),
    ('Generate an image of a futuristic city.', 'es', 'DALL·E', 'image', true, 1),
    ('Translate this text into Spanish.', 'en', 'GPT-3', 'text', true, 1),
    ('Create a voiceover for this podcast script.', 'en', 'VoiceAI', 'voice', false, 1),
    ('What is quantum computing?', 'fr', 'GPT-4', 'text', true, 1),
    ('Generate a painting in the style of Van Gogh.', 'en', 'DALL·E', 'image', false, 1),
    ('What is the current weather in Tokyo?', 'ja', 'GPT-3', 'text', true, 2),
    ('Generate an image of a medieval castle.', 'en', 'DALL·E', 'image', true, 2),
    ('Write a short story about a time-traveling detective.', 'en', 'GPT-3', 'text', true, 2);

