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
(`action`, `permission_id`, `permission_name`, `permission_category_id`)
VALUES
('VIEW_ACCOUNT', 1, 'View account', 1),
('VIEW_ROLE', 2, 'View role', 2),
('VIEW_PERMISSION', 3, 'View permission', 3),
('VIEW_CAMPUS', 4, 'View campus', 4),
('VIEW_SUBJECT', 5, 'View subject', 5),
('VIEW_DEPARTMENT', 6, 'View department', 6),
('VIEW_EXAM', 7, 'View exam', 7),
('VIEW_SCORE', 8, 'View score', 8),

('CREATE_ACCOUNT', 9, 'Create account', 1),
('CREATE_ROLE', 10, 'Create role', 2),
('CREATE_PERMISSION', 11, 'Create permission', 3),
('CREATE_CAMPUS', 12, 'Create campus', 4),
('CREATE_SUBJECT', 13, 'Create subject', 5),
('CREATE_DEPARTMENT', 14, 'Create department', 6),
('CREATE_EXAM', 15, 'Create exam', 7),

('UPDATE_ACCOUNT', 16, 'Update account', 1),
('UPDATE_ROLE', 17, 'Update role', 2),
('UPDATE_PERMISSION', 18, 'Update permission', 3),
('UPDATE_CAMPUS', 19, 'Update campus', 4),
('UPDATE_SUBJECT', 20, 'Update subject', 5),
('UPDATE_DEPARTMENT', 21, 'Update department', 6),
('UPDATE_EXAM', 22, 'Update exam', 7),

('DELETE_ACCOUNT', 23, 'Delete account', 1),
('DELETE_ROLE', 24, 'Delete role', 2),
('DELETE_PERMISSION', 25, 'Delete permission', 3),
('DELETE_CAMPUS', 26, 'Delete campus', 4),
('DELETE_SUBJECT', 27, 'Delete subject', 5),
('DELETE_DEPARTMENT', 28, 'Delete department', 6),
('DELETE_EXAM', 29, 'Delete exam', 7);

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
(true, 2, 'Examiner', 'thanhtuyen66202@gmail.com', 1, '2024-09-30 00:00:00', 1, null, null, null, null),
(true, 3, 'Head of Department', 'thanhtuyen662002@gmail.com', 1, '2024-09-30 00:00:00', 1, null, null, null, null),
(true, 4, 'Lecturer', 'oscarsjoyfuljourney@gmail.com', 1, '2024-09-30 00:00:00', 1, null, null, null, null);

INSERT INTO `account_role`
(`status`, `account_id`, `role_id`)
VALUES
(true, 1, 1),
(true, 2, 2),
(true, 3, 3),
(true, 4, 4);
