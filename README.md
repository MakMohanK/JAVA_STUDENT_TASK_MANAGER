# JAVA_STUDENT_TASK_MANAGER


## File Structure

~~~~java
StudentTaskManager/
│
├── lib/                  # Libraries (e.g., MySQL Connector JAR)
│   └── mysql-connector-java-8.x.xx.jar
│
├── src/                  # Source code folder
│   ├── gui/              # GUI-related Java classes
│   │   ├── LoginPage.java
│   │   ├── SignUpPage.java
│   │   ├── StudentDashboard.java
│   │   └── TeacherDashboard.java
│   │
│   ├── db/               # Database connection and utility classes
│   │   └── DatabaseConnection.java
│   │
│   └── models/           # Models for User and Task
│       ├── User.java
│       └── Task.java
│
├── resources/            # Optional: Configuration files, SQL scripts, etc.
│   ├── schema.sql        # SQL script for creating tables (e.g., `users` and `task`)
│   └── app.properties    # Application settings (e.g., DB credentials, etc.)
│
└── README.md             # Project documentation or guide

~~~~

Student Task Management Application
Overview
The Student Task Management Application is a Java-based desktop application designed to help students and teachers manage tasks effectively. Students can create tasks, view their tasks, and manage their schedule, while teachers can assign and track tasks for students.

Features
User Authentication: Users can sign up and log in as either students or teachers.
Task Management: Students can create tasks and view tasks assigned to them.
Teacher Assignment: Tasks can be assigned to teachers who can then monitor and manage these tasks.
Dashboard: A dedicated dashboard for students to view and manage their tasks.
Prerequisites
Before you begin, ensure you have met the following requirements:

Java Development Kit (JDK) 8 or higher
MySQL database server
MySQL Connector/J JDBC driver
Installation
1. Clone the Repository
bash
Copy code
git clone https://github.com/yourusername/student-task-management.git
cd student-task-management
2. Set Up the Database
Create a MySQL database named task_management.
Import the provided SQL schema into your MySQL database.
sql
Copy code
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    contact VARCHAR(15),
    role ENUM('student', 'teacher') NOT NULL,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE task (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    teacher_id INT NOT NULL,
    description TEXT NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);
3. Configure Database Connection
Update the database connection details in your Java code. Modify the following line in your Java files to match your database configuration:

java
Copy code
Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "your_password");
4. Compile the Java Files
Ensure that the MySQL Connector/J JDBC driver is available in your classpath. Compile the Java files with the following command:

bash
Copy code
javac -cp ../lib/mysql-connector-j-9.0.0.jar gui/*.java db/*.java models/*.java
5. Run the Application
Execute the main class to start the application. For example, to start the LoginPage, use:

bash
Copy code
java -cp ../lib/mysql-connector-j-9.0.0.jar:. gui.LoginPage
Usage
Sign Up: Create a new account by providing a username, name, email, contact number, role (student/teacher), and password.
Log In: Use your credentials to log in to the application.
Dashboard:
Students: View and create tasks assigned to them. See all tasks they have created.
Teachers: (Not yet implemented) Manage tasks assigned to students.
Troubleshooting
Student ID Not Found: Ensure that the student username exists in the database and is correct.
Database Connection Issues: Verify that the database server is running and that your connection details are correct.
Contributing
If you wish to contribute to this project, please fork the repository and submit a pull request. Ensure that your code follows the project's coding style and includes tests.