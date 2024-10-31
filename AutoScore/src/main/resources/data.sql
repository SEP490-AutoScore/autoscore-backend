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
(true, 8, 'MANAGE_SCORE'),
(true, 9, 'MANAGE_EXAM_DATABASE'),
(true, 10, 'MANAGE_GHERKIN_SCENARIO');

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
('VIEW_EXAM_DATABASE', 30, 'View exam database', 9, 1),
('VIEW_GHERKIN_SCENARIO', 34, 'View gherkin scenario', 10, 1),

('CREATE_ACCOUNT', 9, 'Create account', 1, 1),
('CREATE_ROLE', 10, 'Create role', 2, 1),
('CREATE_PERMISSION', 11, 'Create permission', 3, 1),
('CREATE_CAMPUS', 12, 'Create campus', 4, 1),
('CREATE_SUBJECT', 13, 'Create subject', 5, 1),
('CREATE_DEPARTMENT', 14, 'Create department', 6, 1),
('CREATE_EXAM', 15, 'Create exam', 7, 1),
('CREATE_EXAM_DATABASE', 31, 'Create exam database', 9, 1),
('CREATE_GHERKIN_SCENARIO', 35, 'Create gherkin scenario', 10, 1),

('UPDATE_ACCOUNT', 16, 'Update account', 1, 1),
('UPDATE_ROLE', 17, 'Update role', 2, 1),
('UPDATE_PERMISSION', 18, 'Update permission', 3, 1),
('UPDATE_CAMPUS', 19, 'Update campus', 4, 1),
('UPDATE_SUBJECT', 20, 'Update subject', 5, 1),
('UPDATE_DEPARTMENT', 21, 'Update department', 6, 1),
('UPDATE_EXAM', 22, 'Update exam', 7, 1),
('UPDATE_EXAM_DATABASE', 32, 'Update exam database', 9, 1),
('UPDATE_GHERKIN_SCENARIO', 36, 'Update gherkin scenario', 10, 1),

('DELETE_ACCOUNT', 23, 'Delete account', 1, 1),
('DELETE_ROLE', 24, 'Delete role', 2, 1),
('DELETE_PERMISSION', 25, 'Delete permission', 3, 1),
('DELETE_CAMPUS', 26, 'Delete campus', 4, 1),
('DELETE_SUBJECT', 27, 'Delete subject', 5, 1),
('DELETE_DEPARTMENT', 28, 'Delete department', 6, 1),
('DELETE_EXAM', 29, 'Delete exam', 7, 1),
('DELETE_EXAM_DATABASE', 33, 'Delete exam database', 9, 1),
('DELETE_GHERKIN_SCENARIO', 37, 'Delete gherkin scenario', 10, 1);

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
(true, 1, 27),(true, 1, 28),(true, 1, 29),(true, 1, 30),(true, 1, 31),(true, 1, 32),(true, 1, 33),(true, 1, 34),(true, 1, 35),(true, 1, 36),(true, 1, 37),
(true, 2, 1),(true, 2, 4),(true, 2, 5),(true, 2, 6),(true, 2, 7),(true, 2, 8),(true, 2, 9),(true, 2, 12),(true, 2, 13),(true, 2, 14),(true, 2, 15),(true, 2, 16),(true, 2, 19),(true, 2, 20),
(true, 2, 21),(true, 2, 22),(true, 2, 26),(true, 2, 27),(true, 2, 28),(true, 2, 29),(true, 2, 30),(true, 2, 31),(true, 2, 32),(true, 2, 33),(true, 2, 34),(true, 2, 35),(true, 2, 36),(true, 2, 37),
(true, 3, 1),(true, 3, 4),(true, 3, 5),(true, 3, 6),(true, 3, 7),(true, 3, 8),(true, 3, 9),(true, 3, 12);

INSERT INTO `account`
(`account_id`, `email`, `role_id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`, `deleted_by`, `status`)
VALUES
(1, 'tuyenvtse160607@fpt.edu.vn', 1, '2024-09-30 00:00:00', 1, null, null, null, null, true),
(2, 'truonghnse160585@fpt.edu.vn', 1, '2024-09-30 00:00:00', 1, null, null, null, null, true),
(3, 'vuongvtse160599@fpt.edu.vn', 1, '2024-09-30 00:00:00', 1, null, null, null, null, true),
(4, 'minhtpvse160611@fpt.edu.vn', 1, '2024-09-30 00:00:00', 1, null, null, null, null, true),
(5, 'thanhtuyen66202@gmail.com', 2, '2024-09-30 00:00:00', 1, null, null, null, null, true);

INSERT INTO `organization`
(`organization_id`, `name`, `type`, `parent_id`, `status`)
VALUES
(1, 'FPT University', 'UNIVERSITY', null, true),
(2, 'Ho Chi Minh', 'CAMPUS', 1, true),
(3, 'Ha Noi', 'CAMPUS', 1, true),
(4, 'Da Nang', 'CAMPUS', 1, true),
(5, 'Can Tho', 'CAMPUS', 1, true),
(6, 'SE', 'MAJOR', 2, true),
(7, 'SE', 'MAJOR', 3, true),
(8, 'SE', 'MAJOR', 4, true),
(9, 'SE', 'MAJOR', 5, true),
(10, 'JAVA', 'DEPARTMENT', 6, true),
(11, '.NET', 'DEPARTMENT', 6, true),
(12, 'JAVA', 'DEPARTMENT', 7, true),
(13, '.NET', 'DEPARTMENT', 7, true),
(14, 'JAVA', 'DEPARTMENT', 8, true),
(15, '.NET', 'DEPARTMENT', 8, true),
(16, 'JAVA', 'DEPARTMENT', 9, true),
(17, '.NET', 'DEPARTMENT', 9, true);

INSERT INTO `account_organization`
(`status`, `account_id`, `organization_id`)
VALUES
(true, 1, 1),
(true, 2, 1),
(true, 3, 1),
(true, 4, 1),
(true, 1, 2),
(true, 2, 2),
(true, 3, 2),
(true, 4, 2),
(true, 5, 2),
(true, 5, 11);

INSERT INTO `position`
(`position_id`, `name`, `status`)
VALUES
(1, 'Head of Department', true),
(2, 'Examiner', true),
(3, 'Lecturer', true),
(4, 'Admin', true);

INSERT INTO `employee`
(`employee_id`, `full_name`, `employee_code`, `account_id`, `position_id`, `organization_id`, `ai_prompt_id`, `status`)
VALUES
(1, 'Võ Thanh Tuyền', 'AD0001', 1, 4, 1, null, true),
(2, 'Hà Nhật Trường', 'AD0002', 2, 4, 1, null, true),
(3, 'Võ Trọng Vương', 'AD0003', 3, 4, 1, null, true),
(4, 'Thiều Phan Văn Minh', 'AD0004', 4, 4, 1, null, true),
(5, 'Võ Thanh Tuyền', 'AD0005', 5, 2, 2, null, true);

INSERT INTO `subject` 
(`subject_name`, `subject_code`, `status`, `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`, `deleted_by`) 
VALUES 
('c# coding', 'PRN231', true, '2023-09-30 09:00:00', 1, null, null, null, null),
('java coding', 'JAVA231', true, '2023-09-30 09:00:00', 1, null, null, null, null);

INSERT INTO `organization_subject`
(`status`, `organization_id`, `subject_id`)
VALUES
(true, 2, 1),
(true, 11, 1),
(true, 2, 2),
(true, 10, 2);

INSERT INTO `semester`
(`semester_code`, `semester_name`, `status`)
VALUES
('SP24', 'Spring 2024', true),
('SU24', 'Summer 2024', true),
('FA24', 'Fall 2024', true);

INSERT INTO `exam` 
(`exam_code`, `exam_at`, `grading_at`, `publish_at`, `semester_id`, `status`, `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`, `deleted_by`, `subject_id`) 
VALUES 
('PRN231_SP24_PE', '2024-10-01 10:00:00', '2024-10-02 15:00:00', '2024-10-03 12:00:00', 2, true, '2024-09-30 09:00:00', 1, null, null, null, null, 1),
('PRN231_SU24_PE', '2024-11-01 10:00:00', '2024-11-02 15:00:00', '2024-11-03 12:00:00', 2, true, '2024-09-30 09:00:00', 2, null, null, null, null, 1),
('PRN231_FA24_PE', '2024-12-01 10:00:00', '2024-12-02 15:00:00', '2024-12-03 12:00:00', 2, true, '2024-09-30 09:00:00', 3, null, null, null, null, 1);

-- INSERT INTO `instructions` 
-- (`introduction`, `important`, `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`, `deleted_by`, `subject_id`) 
-- VALUES 
-- ('You are NOT allowed to use any device to share data with others.
-- You must use Visual Studio 2019 or above, MSSQL Server 2012 or above for your development tools. ', '1.	Create Solution/Project in Visual Studio named PE_PRN231_FA24_TrialTest_StudentFullname_BE for API, and PE_PRN231_FA24_TrialTest_StudentCode_FE for Client Application. Set the default Client application for your project as Login page.', '2024-09-30 10:00:00', 1, null, null, null, null, 1),
-- ('Introduction 2', 'Important 2', '2024-09-30 10:05:00', 1, null, null, null, null, 1),
-- ('Introduction 3', 'Important 3', '2024-09-30 10:10:00', 1, null, null, null, null, 1);

INSERT INTO `exam_paper` 
(`exam_paper_code`, `status`, `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`, `deleted_by`, `exam_id`) 
VALUES 
('PRN234_PE_SU24', true, '2024-10-30 10:00:00', 1, null, null, null, null, 2),
('PRN234_PE_FA24', true, '2024-09-30 10:00:00', 1, null, null, null, null, 2),
('PRN234_PE_SP25', true, '2024-09-30 10:00:00', 1, null, null, null, null, 2);

INSERT INTO `exam_question`
(`question_content`, `order_by`, `exam_question_score`, `status`, `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`, `deleted_by`, `exam_paper_id`)
VALUES
('1.	Check authentication/authorization with the ASP.NET Core Web API with JSON Web Token (JWT)', 1, 2.5, true, NOW(), 1, NULL, NULL, NULL, NULL, 1),
('2.	You must use RESTful API to implement the ASP.NET Core Web API. CORS is using in this case.', 2, 2.5, true, NOW(), 1, NULL, NULL, NULL, NULL, 1),
('3.	Check authentication/authorization with the ASP.NET Core Web API with JSON Web Token (JWT)', 3, 2.5, true, NOW(), 1, NULL, NULL, NULL, NULL, 1),
('4.	You must use RESTful API to implement the ASP.NET Core Web API. CORS is using in this case.', 4, 2.5, true, NOW(), 1, NULL, NULL, NULL, NULL, 1);

INSERT INTO `postman_for_grading`
(`score_of_function`, `exam_question_id`, `order_by`, `postman_for_grading_parent_id`, `total_pm_test`, `postman_function_name`)
VALUES
(2.5, 1, 1, null, 2, 'login'),
(2.5, 1, 2, 1, 4, 'get'),
(2.5, 1, 3, 1, 2, 'post'),
(2.5, 1, 4, 3, 2, 'delete');

-- INSERT INTO `autoscore`.`exam_barem` 
-- (`barem_max_score`, `order_by`, `status`, `exam_question_id`, `allow_role`, `barem_function`, `endpoint`, `error_response`, `method`, `payload`, `payload_type`, `success_response`, `validation`, `barem_content`) 
-- VALUES (
--   2, 
--   1, 
--   true, 
--   1, 
--   'Administrator, Patients, Doctor', 
--   'Authenticates the user using their email and password. On successful authentication, the API returns a token (JWT or session token) that can be used for further authenticated requests.', 
--   '/api/login', 
--   'Response Code: 401 Unauthorized (for incorrect email/password)\nResponse Body (JSON):\n{ "error": "Invalid email or password" }', 
--   'POST', 
--   '{
--   "email": "user@example.com",
--   "password": "yourpassword"
--   }', 
--   'Request Body (JSON)', 
--   'Response Code: 200 OK\nResponse Body (JSON):\n{ "message": "Login successful", "token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c", "user": { "id": 1, "email": "user@example.com", "role": "admin" }}', 
--   'Email: Must be a valid email address format. Password: Should meet security requirements (e.g., minimum length, complexity), but this check is typically handled during user registration.', 
--   'Login function'
-- ),
-- (2,
--  1,
--  true,
--  2,
--  'Administrator, Patients, Doctor',
--  'Adds a new person and, if applicable, the viruses they are infected with.',
--  '/api/person',
--  'Response Code: 401 Unauthorized (for incorrect email/password)\nResponse Body (JSON):\n{ "error": "Invalid email or password" }',
--  'POST',
--  '{\n
--                   \"personID\": 1,\n
--                    \"fullName\": \"John Doe\",\n
--                     \"birthDay\": \"1990-05-15\",\n
--                     \"phone\": \"1234567890\",\n
--                     \"viruses\": [\n
--                         { \n" +
--                            \"virusName\": \"COVID-19\",\n
--                            \"resistanceRate\": 0.2 \n
--                         }, \n
--                        { \n
--                             \"virusName\": \"Influenza\",\n
--                             \"resistanceRate\": 0.0 \n
--                         } \n
--                    ]\n
--                 }',
--  'Request Body (JSON)',
--  'Response: 201 Created\nResponse Body (JSON):\\n{ "personId": 1, "message": "Person and viruses added successfully" }',
--  'Email: Must be a valid email address format.\\nPassword: Should meet security requirements (e.g., minimum length, complexity), but this check is typically handled during user registration.',
--  'Create (Add a person and the viruses they are infected with)'
-- );


