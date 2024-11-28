INSERT INTO `permission_category`
(`status`, `permission_category_id`, `permission_category_name`)
VALUES
(true, 1, 'Account Management'),
(true, 2, 'Role Management'),
(true, 3, 'Permission Management'),
(true, 4, 'Organization Management'),
(true, 5, 'Subject Management'),
(true, 6, 'Department Management'),
(true, 7, 'Exam Management'),
(true, 8, 'Score Management'),
(true, 9, 'Other');

INSERT INTO `permission`
(`action`, `permission_id`, `permission_name`, `permission_category_id`, `status`, `description`)
VALUES
-- Account Management
('VIEW_ACCOUNT', 1, 'View account', 1, 1, 'Allows viewing details of accounts'),
('CREATE_ACCOUNT', 9, 'Create account', 1, 1, 'Allows creating new accounts'),
('UPDATE_ACCOUNT', 16, 'Update account', 1, 1, 'Allows updating account information'),
('DELETE_ACCOUNT', 23, 'Delete account', 1, 1, 'Allows deleting accounts'),

-- Role Management
('VIEW_ROLE', 2, 'View role', 2, 1, 'Allows viewing role details'),
('CREATE_ROLE', 10, 'Create role', 2, 1, 'Allows creating new roles'),
('UPDATE_ROLE', 17, 'Update role', 2, 1, 'Allows updating role information'),
('DELETE_ROLE', 24, 'Delete role', 2, 1, 'Allows deleting roles'),

-- Permission Management
('VIEW_PERMISSION', 3, 'View permission', 3, 1, 'Allows viewing permission settings'),
('CREATE_PERMISSION', 11, 'Create permission', 3, 1, 'Allows creating new permissions'),
('UPDATE_PERMISSION', 18, 'Update permission', 3, 1, 'Allows updating existing permissions'),
('DELETE_PERMISSION', 25, 'Delete permission', 3, 1, 'Allows deleting permissions'),

-- Organization Management
('VIEW_CAMPUS', 4, 'View campus', 4, 1, 'Allows viewing campus details'),
('CREATE_CAMPUS', 12, 'Create campus', 4, 1, 'Allows creating new campuses'),
('UPDATE_CAMPUS', 19, 'Update campus', 4, 1, 'Allows updating campus information'),
('DELETE_CAMPUS', 26, 'Delete campus', 4, 1, 'Allows deleting campuses'),

-- Subject Management
('VIEW_SUBJECT', 5, 'View subject', 5, 1, 'Allows viewing subject details'),
('CREATE_SUBJECT', 13, 'Create subject', 5, 1, 'Allows creating new subjects'),
('UPDATE_SUBJECT', 20, 'Update subject', 5, 1, 'Allows updating subject information'),
('DELETE_SUBJECT', 27, 'Delete subject', 5, 1, 'Allows deleting subjects'),

-- Department Management
('VIEW_DEPARTMENT', 6, 'View department', 6, 1, 'Allows viewing department details'),
('CREATE_DEPARTMENT', 14, 'Create department', 6, 1, 'Allows creating new departments'),
('UPDATE_DEPARTMENT', 21, 'Update department', 6, 1, 'Allows updating department information'),
('DELETE_DEPARTMENT', 28, 'Delete department', 6, 1, 'Allows deleting departments'),

-- Exam Management
('VIEW_EXAM', 7, 'View exam', 7, 1, 'Allows viewing exam details'),
('CREATE_EXAM', 15, 'Create exam', 7, 1, 'Allows creating new exams'),
('UPDATE_EXAM', 22, 'Update exam', 7, 1, 'Allows updating exam information'),
('DELETE_EXAM', 29, 'Delete exam', 7, 1, 'Allows deleting exams'),

-- Exam Database
('VIEW_EXAM_DATABASE', 30, 'View exam database', 7, 1, 'Allows viewing exam database details'),
('CREATE_EXAM_DATABASE', 31, 'Create exam database', 7, 1, 'Allows creating new exam database entries'),
('UPDATE_EXAM_DATABASE', 32, 'Update exam database', 7, 1, 'Allows updating exam database entries'),
('DELETE_EXAM_DATABASE', 33, 'Delete exam database', 7, 1, 'Allows deleting exam database entries'),

-- Gherkin Scenario
('VIEW_GHERKIN_SCENARIO', 34, 'View gherkin scenario', 7, 1, 'Allows viewing Gherkin scenario details'),
('CREATE_GHERKIN_SCENARIO', 35, 'Create gherkin scenario', 7, 1, 'Allows creating new Gherkin scenarios'),
('UPDATE_GHERKIN_SCENARIO', 36, 'Update gherkin scenario', 7, 1, 'Allows updating Gherkin scenarios'),
('DELETE_GHERKIN_SCENARIO', 37, 'Delete gherkin scenario', 7, 1, 'Allows deleting Gherkin scenarios'),

-- Score Management
('VIEW_SCORE', 8, 'View score', 8, 1, 'Allows viewing scores'),
('EXPORT_SCORE', 38, 'Export score', 8, 1, 'Allows exporting score reports'),

-- Dashboard
('DASHBOARD', 39, 'Dashboard access', 9, 1, 'Allows access to the dashboard'),

-- Student
('VIEW_STUDENT', 40, 'View student', 7, 1, 'Allows viewing student details'),
('IMPORT_STUDENT', 41, 'Import student', 7, 1, 'Allows importing student data');
INSERT INTO `role`
(`status`, `role_id`, `role_code`, `role_name`, `description`, `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`, `deleted_by`)
VALUES
(true, 1, 'ADMIN', 'Administrator', 'Responsible for managing the system and users', '2024-09-30 00:00:00', 1, null, null, null, null),
(true, 2, 'EXAMINER', 'Examiner', 'Responsible for evaluating exams and assessments', '2024-09-30 00:00:00', 1, null, null, null, null),
(true, 3, 'HEAD_OF_DEPARTMENT', 'Head of Department', 'Leads a department and oversees its operations', '2024-09-30 00:00:00', 1, null, null, null, null),
(true, 4, 'LECTURER', 'Lecturer', 'Create tests and assignments for students and schools', '2024-09-30 00:00:00', 1, null, null, null, null);

INSERT INTO `role_permission`
(`status`, `role_id`, `permission_id`)
VALUES
-- Admin
(true, 1, 1), (true, 1, 2), (true, 1, 3), (true, 1, 4), (true, 1, 5), (true, 1, 6), (true, 1, 7), (true, 1, 8), (true, 1, 9), (true, 1, 10),
(true, 1, 11), (true, 1, 12), (true, 1, 13), (true, 1, 14), (true, 1, 15), (true, 1, 16), (true, 1, 17), (true, 1, 18), (true, 1, 19), (true, 1, 20),
(true, 1, 21), (true, 1, 22), (true, 1, 23), (true, 1, 24), (true, 1, 25), (true, 1, 26), (true, 1, 27), (true, 1, 28), (true, 1, 29), (true, 1, 30),
(true, 1, 31), (true, 1, 32), (true, 1, 33), (true, 1, 34), (true, 1, 35), (true, 1, 36), (true, 1, 37), (true, 1, 38), (true, 1, 39), (true, 1, 40), (true, 1, 41),
-- Examiner
(false, 2, 2), (false, 2, 3), (false, 2, 10), (false, 2, 11), (false, 2, 17), (false, 2, 18), (false, 2, 23), (false, 2, 24), (false, 2, 25), (false, 2, 39),
(true, 2, 1), (true, 2, 4), (true, 2, 5), (true, 2, 6), (true, 2, 7), (true, 2, 8), (true, 2, 9), (true, 2, 12), (true, 2, 13), (true, 2, 14),
(true, 2, 15), (true, 2, 16), (true, 2, 19), (true, 2, 20), (true, 2, 21), (true, 2, 22), (true, 2, 26), (true, 2, 27), (true, 2, 28), (true, 2, 29),
(true, 2, 30), (true, 2, 31), (true, 2, 32), (true, 2, 33), (true, 2, 34), (true, 2, 35), (true, 2, 36), (true, 2, 37), (true, 2, 38), (true, 2, 39), (true, 2, 40), (true, 2, 41),
-- Head of Department
(false, 3, 2), (false, 3, 3), (false, 3, 10), (false, 3, 11), (false, 3, 17), (false, 3, 18), (false, 3, 23), (false, 3, 24), (false, 3, 25), (false, 3, 26), 
(false, 3, 15), (false, 3, 16), (false, 3, 19), (false, 3, 20), (false, 3, 21), (false, 3, 22), (false, 3, 27), (false, 3, 28),(false, 3, 29), (false, 3, 30), 
(false, 3, 31), (false, 3, 32), (false, 3, 33), (false, 3, 34), (false, 3, 35), (false, 3, 36), (false, 3, 37), (false, 3, 38), (false, 3, 39),
(true, 3, 1), (true, 3, 4), (true, 3, 5), (true, 3, 6), (true, 3, 7), (true, 3, 8), (true, 3, 9), (true, 3, 12), (true, 3, 13), (true, 3, 14), (true, 3, 40), (true, 3, 41),
-- Lecturer
(false, 3, 2), (false, 3, 3), (false, 3, 10), (false, 3, 11), (false, 3, 17), (false, 3, 18), (false, 3, 23), (false, 3, 24), (false, 3, 25), (false, 3, 26), 
(false, 3, 15), (false, 3, 16), (false, 3, 19), (false, 3, 20), (false, 3, 21), (false, 3, 22), (false, 3, 27), (false, 3, 28),(false, 3, 29), (false, 3, 30), 
(false, 3, 31), (false, 3, 32), (false, 3, 33), (false, 3, 34), (false, 3, 35), (false, 3, 36), (false, 3, 37), (false, 3, 38), (false, 3, 39),
(true, 3, 1), (true, 3, 4), (true, 3, 5), (true, 3, 6), (true, 3, 7), (true, 3, 8), (true, 3, 9), (true, 3, 12), (true, 3, 13), (true, 3, 14), (true, 3, 40), (true, 3, 41);

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
(1, 'FPTU', 'UNIVERSITY', null, true),
(2, 'HoChiMinh', 'CAMPUS', 1, true),
(3, 'HaNoi', 'CAMPUS', 1, true),
(4, 'DaNang', 'CAMPUS', 1, true),
(5, 'CanTho', 'CAMPUS', 1, true),
(18, 'QuyNhon', 'CAMPUS', 1, true),
(6, 'SE', 'MAJOR', 2, true),
(7, 'SE', 'MAJOR', 3, true),
(8, 'SE', 'MAJOR', 4, true),
(9, 'SE', 'MAJOR', 5, true),
(19, 'SE', 'MAJOR', 18, true),
(10, 'JAVA', 'DEPARTMENT', 6, true),
(11, '.NET', 'DEPARTMENT', 6, true),
(12, 'JAVA', 'DEPARTMENT', 7, true),
(13, '.NET', 'DEPARTMENT', 7, true),
(14, 'JAVA', 'DEPARTMENT', 8, true),
(15, '.NET', 'DEPARTMENT', 8, true),
(16, 'JAVA', 'DEPARTMENT', 9, true),
(17, '.NET', 'DEPARTMENT', 9, true),
(20, 'JAVA', 'DEPARTMENT', 19, true),
(21, '.NET', 'DEPARTMENT', 19, true);

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
(4, 'Software Engineering ', true);

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
(`exam_code`, `exam_at`, `grading_at`, `publish_at`, `semester_id`, `status`, `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`, `deleted_by`, `subject_id`, `type`) 
VALUES 
('PRN231_FA24_PE', '2024-10-01 10:00:00', '2024-10-02 15:00:00', '2024-10-03 12:00:00', 2, true, '2024-09-30 09:00:00', 1, null, null, null, null, 1, 'EXAM'),
('PRN231_SU24_PE', '2024-11-01 10:00:00', '2024-11-02 15:00:00', '2024-11-03 12:00:00', 2, true, '2024-09-30 09:00:00', 2, null, null, null, null, 1, 'EXAM'),  
('PRN231_SU24_ASS', '2024-12-01 10:00:00', '2024-12-02 15:00:00', '2024-12-03 12:00:00', 2, true, '2024-09-30 09:00:00', 3, null, null, null, null, 1, 'ASSIGNMENT');

-- INSERT INTO `instructions` 
-- (`introduction`, `important`, `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`, `deleted_by`, `subject_id`) 
-- VALUES 
-- ('You are NOT allowed to use any device to share data with others.
-- You must use Visual Studio 2019 or above, MSSQL Server 2012 or above for your development tools. ', '1.	Create Solution/Project in Visual Studio named PE_PRN231_FA24_TrialTest_StudentFullname_BE for API, and PE_PRN231_FA24_TrialTest_StudentCode_FE for Client Application. Set the default Client application for your project as Login page.', '2024-09-30 10:00:00', 1, null, null, null, null, 1),
-- ('Introduction 2', 'Important 2', '2024-09-30 10:05:00', 1, null, null, null, null, 1),
-- ('Introduction 3', 'Important 3', '2024-09-30 10:10:00', 1, null, null, null, null, 1);

INSERT INTO `exam_paper` 
(`exam_paper_code`, `status`, `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`, `deleted_by`, `exam_id`,`is_used`, `subject_id`, `instruction`) 
VALUES 
('009909', 'COMPLETE', '2024-10-30 10:00:00', 1, null, null, null, null, 1,true,1,`Instruction`),
('123456', 'COMPLETE', '2024-09-30 10:00:00', 1, null, null, null, null, 2,true,1, `Instruction`),
('456789', 'COMPLETE', '2024-09-30 10:00:00', 1, null, null, null, null, 2,false,1, `Instruction`);

INSERT INTO `Exam_Question`
(`question_content`, `exam_question_score`, `end_point`, `role_allow`, `http_method`, `description`, `payload_type`, `payload`, `validation`, `sucess_response`, `error_response`, `status`, `order_by`, `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`, `deleted_by`, `exam_paper_id`)
VALUES
('Login', 2, '/api/login', 'Administrator, Doctor, Patient', 'POST', 'This function allows the user to access the system', 'JSON', '{
  "email": "user@example.com",
  "password": "securepassword123"
}', 'Check validation of email and password cannot be null', '
        Response Code: 200 OK
		Response Body (JSON):
{ 
"message": "Login successful", 
"token": "example token", 
"user": { 
"id": 1, 
"email": "user@example.com", 
"role": "admin" 
} 
}
', 'Response Code: 401 Unauthorized (for incorrect email/password)
		Response Body (JSON):
		{ "error": "Invalid email or password" }
', true, 1, '2023-10-01 10:00:00', 3, NULL, NULL, NULL, NULL, 1),
('Create (Add a person and the viruses they are infected with)', 2, '/api/person', 'Administrator, Doctor, Patient', 'POST', 'Retrieves details of a person, including any viruses they are infected with.', 'JSON',
'{ 
  "personID": 1,
  "fullName": "John Doe",
  "birthDay": "1990-05-15",
  "phone": "1234567890",
  "viruses": [
    { 
      "virusName": "COVID-19",
      "resistanceRate": 0.2 
    }, 
    { 
      "virusName": "Influenza",
      "resistanceRate": 0.0 
    } 
  ] 
}', 
'',
'
Response: 201 Created
{ 
"personId": 1, 
"message": "Person and viruses added successfully"
 }',
 'Response Code: 500 (for incorrect phone/viruses)
		Response Body (JSON):
		{ "error": "Invalid viruses or phone" }
', true, 2, '2023-10-01 10:00:00', 3, NULL, NULL, NULL, NULL, 1),
('Read (Retrieve person details and associated viruses)', 2, '/api/person/{id}', 'Administrator, Doctor, Patient', 'GET', 'Retrieves details of a person, including any viruses they are infected with.', 'URL Parameters',
'id (ID of the person to retrieve)
',
'',
'
Response: 200 OK
{ 
"personId": 1,
 "fullName": "John Doe", 
"birthDay": "1990-05-15",
"phone": "1234567890", 
"viruses":
 [
    { 
      "virusName": "COVID-19", 
      "resistanceRate": 0.2 
    }, 
    { 
      "virusName": "Influenza", 
      "resistanceRate": 0.0 
    }
 ] 
}
',
 '', true, 3, '2023-10-01 10:00:00', 3, NULL, NULL, NULL, NULL, 1),
('Retrieve all persons and their viruses', 2, '/api/persons', 'Administrator, Doctor, Patient', 'GET', 'Retrieve all persons and their viruses', NULL, NULL, NULL,
'[ 
    { 
      "personId": 1, 
      "fullName": "John Doe", 
      "birthDay": "1990-05-15", 
      "phone": "1234567890", 
      "viruses": 
      [ 
        { 
          "virusName": "COVID-19", 
          "resistanceRate": 0.2 
        }
      ]
    },
    { 
      "personId": 2,
      "fullName": "Jane Smith", 
      "birthDay": "1985-10-22", 
      "phone": "0987654321", 
      "viruses": [] 
    }
 ]', 
 'Response Code: 500', true, 4, '2023-10-01 10:00:00', 3, NULL, NULL, NULL, NULL, 1),
('Update (Update person details and their viruses)', 2, '/api/person/{id}', 'Doctor', 'PUT', 'Updates the details of a person, including their associated viruses.', 'URL Parameters', '
id (ID of the person to update)
Request Body (JSON):
{ 
  "fullName": "Jonathan Doe", 
  "birthDay": "1990-05-15", 
  "phone": "1234567890", 
  "viruses": [
    { 
      "virusName": "COVID-19", 
      "resistanceRate": 0.5 
    }, 
    { 
      "virusName": "Influenza", 
      "resistanceRate": 0.1 
    } 
  ] 
}', NULL, 'Response: 200 OK
{ "message": "Person and viruses updated successfully" }
', NULL, true, 5, '2023-10-01 10:00:00', 3, NULL, NULL, NULL, NULL, 1),
('Delete (Delete a person and their associated viruses)', 2, '/api/person/{id}', 'Doctor', 'DELETE', 'Deletes a person and their relationship with any viruses they are infected with.', 'URL Parameters', 'id (ID of the person to delete)', NULL, 'Response: 200 OK
{ "message": "Person and related viruses deleted successfully" }
', NULL, true, 6, '2023-10-01 10:00:00', 3, NULL, NULL, NULL, NULL, 1);



-- INSERT INTO `postman_for_grading`
-- (`score_of_function`, `exam_question_id`, `order_by`, `postman_for_grading_parent_id`, `total_pm_test`, `postman_function_name`, `exam_paper_id`, `status`)
-- VALUES
-- (2, 1, 1, null, 2, 'login',1,true),
-- (2, 2, 2, 1, 2, 'login fail',1,true),
-- (2, 1, 3, 1, 4, 'get',1,true),
-- (2, 3, 4, 1, 4, 'get id',1,true),
-- (2, 2, 5, 1, 3, 'delete',1,true);

INSERT INTO `ai_api_key`
(`ai_api_key`, `ai_name`, `account_id`,`status`,`shared`)
VALUES
('AIzaSyDxNBkQgMw5bxnB47_NLI5dnmiwKoRPqJc', 'Gemini',1,true,true),
('AIzaSyDxNBkQgMw5bxnB47_NLI5dnmiwKoRPqJc', 'Gemini',2,true,true),
('AIzaSyDxNBkQgMw5bxnB47_NLI5dnmiwKoRPqJc', 'Gemini',3,true,true),
('AIzaSyDxNBkQgMw5bxnB47_NLI5dnmiwKoRPqJc', 'Gemini',4,true,true),
('AIzaSyChK5Jo_vP3JM2xeCALY_QXLuCkoad-y5U', 'Gemini',5,true,true);

INSERT INTO `account_selected_key`
(`account_id`, `selected_ai_api_key_id`)
VALUES
(3, 1),
(1, 1),
(2, 1),
(4, 1);


INSERT INTO `content`
(`question_ask_ai_content`, `order_priority`,`purpose`)
VALUES
('This is Database, Save to your memory, do not reply
', 1,'GENERATE_GHERKIN_FORMAT'),
('Generate Gherkin format scenarios for the given feature or API. Ensure that each complete scenario is fully enclosed in double curly braces {{ }}, regardless of whether optional steps are included. If multiple roles are involved, create separate scenarios for each role to explicitly represent their unique context and actions. Use the following structure as a guideline:
{{ 
Scenario: [Brief and clear description of the scenario]
  Given [Describe the initial condition or prerequisite]
  And [Optional additional conditions or prerequisites, if applicable]
  When [Describe the key action or event that occurs]
  And [Optional additional actions or events, if applicable]
  Then [Describe the primary outcome or result]
  And [Optional additional outcomes or verifications, if applicable]
}}
', 2,'GENERATE_GHERKIN_FORMAT'),
('Save to your memory, do not reply
', 1,'GENERATE_GHERKIN_FORMAT_MORE'),
('Generate Gherkin format scenarios for the given feature or API. Ensure that each complete scenario is fully enclosed in double curly braces {{ }}, regardless of whether optional steps are included. If multiple roles are involved, create separate scenarios for each role to explicitly represent their unique context and actions. Use the following structure as a guideline: '
,2,'GENERATE_GHERKIN_FORMAT_MORE'),

('Save to your memory, do not reply
', 1,'GENERATE_POSTMAN_COLLECTION'),
('Write JSON Postman collection for 1 item in Gherkin format below, with no explanation. Provide only the JSON structure. The `event.script.exec` section in `item` must contain valid Postman test scripts (using pm.test). Write a JSON Postman collection that contains `info` and `item`. The `info` section should include `_postman_id`, `name`, `schema`, `_exporter_id`. The `item` section should include `name`, `event.listen` with the value `test`, and `event.script.exec` with the value `pm.test`. Do not explain the JSON structure, just provide the raw JSON.
', 2,'GENERATE_POSTMAN_COLLECTION'),

('This is Database, Save to your memory, do not reply', 1,'GENERATE_POSTMAN_COLLECTION_MORE'),
('Given the JSON Postman collection below, please add multiple pm.test scripts within the same test case. Ensure that each test script is inside the same `event.script.exec` block, but each script checks a different condition or assertion. In the provided Postman JSON, within the `event.script.exec` section, please create multiple `pm.test` scripts under the same test case. Each `pm.test` should check a separate condition without creating additional test cases.', 2,'GENERATE_POSTMAN_COLLECTION_MORE');


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

INSERT INTO `autoscore`.`important`
(`subject_id`,
`important_code`,
`important_name`)
VALUES
(1,
'CNS',
'Check connection String of the appsetting'),
(1,
'SLN',
'Check solution name'),
(1,
'SST',
'Check source structure');

INSERT INTO `autoscore`.`important_exam_paper` (`status`, `exam_paper_id`, `important_exam_paper_id`, `important_id`) VALUES ('1', '1', '1', '1');
INSERT INTO `autoscore`.`important_exam_paper` (`status`, `exam_paper_id`, `important_exam_paper_id`, `important_id`) VALUES ('1', '1', '2', '2');



