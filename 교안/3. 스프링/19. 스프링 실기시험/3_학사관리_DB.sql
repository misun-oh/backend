-- 학사관리 실기시험 제공 DB 스크립트
-- MySQL 8.x 기준

CREATE DATABASE IF NOT EXISTS academic
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE academic;

DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS department;

CREATE TABLE department (
    department_id VARCHAR(20) PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL
);

CREATE TABLE student (
    student_id VARCHAR(20) PRIMARY KEY,
    student_name VARCHAR(50) NOT NULL,
    department_id VARCHAR(20) NOT NULL,
    grade INT NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(30),
    status VARCHAR(20) NOT NULL,
    admission_date DATE NOT NULL,
    CONSTRAINT fk_student_department
        FOREIGN KEY (department_id) REFERENCES department(department_id)
);

INSERT INTO department (department_id, department_name) VALUES
    ('CS', '컴퓨터공학과'),
    ('BUSINESS', '경영학과'),
    ('DESIGN', '디자인학과');

INSERT INTO student
    (student_id, student_name, department_id, grade, email, phone, status, admission_date)
VALUES
    ('20240001', '김학생', 'CS', 2, 'student@example.com', '010-1234-5678', 'ENROLLED', '2024-03-01'),
    ('20230015', '이수강', 'BUSINESS', 3, 'lee@example.com', '010-2345-6789', 'ENROLLED', '2023-03-01'),
    ('20220007', '박휴학', 'DESIGN', 4, 'park@example.com', '010-3456-7890', 'LEAVE', '2022-03-01'),
    ('20210021', '최졸업', 'CS', 4, 'choi@example.com', '010-4567-8901', 'GRADUATED', '2021-03-01'),
    ('20240022', '정학생', 'DESIGN', 2, 'jung@example.com', '010-5678-9012', 'ENROLLED', '2024-03-01'),
    ('20230031', '한학생', 'BUSINESS', 3, 'han@example.com', '010-6789-0123', 'ENROLLED', '2023-03-01'),
    ('20220042', '오학생', 'CS', 4, 'oh@example.com', '010-7890-1234', 'LEAVE', '2022-03-01'),
    ('20240058', '서학생', 'DESIGN', 2, 'seo@example.com', '010-8901-2345', 'ENROLLED', '2024-03-01'),
    ('20230064', '윤학생', 'CS', 3, 'yoon@example.com', '010-9012-3456', 'ENROLLED', '2023-03-01'),
    ('20220073', '강학생', 'BUSINESS', 4, 'kang@example.com', '010-0123-4567', 'GRADUATED', '2022-03-01'),
    ('20240086', '임학생', 'CS', 2, 'im@example.com', '010-1234-6789', 'ENROLLED', '2024-03-01'),
    ('20230097', '문학생', 'DESIGN', 3, 'moon@example.com', '010-2345-7890', 'ENROLLED', '2023-03-01');
