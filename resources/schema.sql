CREATE DATABASE IF NOT EXISTS task_management;

USE task_management;

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE,
    name VARCHAR(100),
    email VARCHAR(100),
    contact VARCHAR(15),
    role ENUM('teacher', 'student'),
    password VARCHAR(100)
);

CREATE TABLE task (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT,
    teacher_id INT,
    description TEXT,
    start_time DATETIME,
    end_time DATETIME,
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);
