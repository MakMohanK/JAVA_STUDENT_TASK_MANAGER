package gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskCreationDialogTeacher extends JDialog {
    private JTextField descriptionField;
    private JSpinner startDateSpinner, startTimeSpinner;
    private JSpinner endDateSpinner, endTimeSpinner;
    private JComboBox<String> studentComboBox;
    private JButton createButton, cancelButton;
    private String teacherName;

    public TaskCreationDialogTeacher(JFrame parent, String teacherName) {
        super(parent, "Create Task", true);
        this.teacherName = teacherName;

        setLayout(null);
        setSize(450, 400);
        setLocationRelativeTo(parent);

        JLabel descriptionLabel = new JLabel("Task Description:");
        descriptionLabel.setBounds(20, 20, 150, 25);
        add(descriptionLabel);

        descriptionField = new JTextField();
        descriptionField.setBounds(180, 20, 150, 25);
        add(descriptionField);

        // Start Date and Time pickers
        JLabel startDateLabel = new JLabel("Start Date:");
        startDateLabel.setBounds(20, 60, 150, 25);
        add(startDateLabel);

        startDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        startDateSpinner.setEditor(startDateEditor);
        startDateSpinner.setBounds(180, 60, 150, 25);
        add(startDateSpinner);

        JLabel startTimeLabel = new JLabel("Start Time:");
        startTimeLabel.setBounds(20, 100, 150, 25);
        add(startTimeLabel);

        startTimeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startTimeEditor = new JSpinner.DateEditor(startTimeSpinner, "HH:mm:ss");
        startTimeSpinner.setEditor(startTimeEditor);
        startTimeSpinner.setBounds(180, 100, 150, 25);
        add(startTimeSpinner);

        // End Date and Time pickers
        JLabel endDateLabel = new JLabel("End Date:");
        endDateLabel.setBounds(20, 140, 150, 25);
        add(endDateLabel);

        endDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        endDateSpinner.setEditor(endDateEditor);
        endDateSpinner.setBounds(180, 140, 150, 25);
        add(endDateSpinner);

        JLabel endTimeLabel = new JLabel("End Time:");
        endTimeLabel.setBounds(20, 180, 150, 25);
        add(endTimeLabel);

        endTimeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endTimeEditor = new JSpinner.DateEditor(endTimeSpinner, "HH:mm:ss");
        endTimeSpinner.setEditor(endTimeEditor);
        endTimeSpinner.setBounds(180, 180, 150, 25);
        add(endTimeSpinner);

        // Student selection dropdown
        JLabel studentLabel = new JLabel("Assign Student:");
        studentLabel.setBounds(20, 220, 150, 25);
        add(studentLabel);

        studentComboBox = new JComboBox<>();
        studentComboBox.setBounds(180, 220, 150, 25);
        add(studentComboBox);
        populateStudentComboBox();

        createButton = new JButton("Create");
        createButton.setBounds(100, 300, 100, 30);
        add(createButton);

        cancelButton = new JButton("Cancel");
        cancelButton.setBounds(220, 300, 100, 30);
        add(cancelButton);

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createTask();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        setVisible(true);
    }

    private void populateStudentComboBox() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234");
            String query = "SELECT username FROM users WHERE role = 'student'";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                studentComboBox.addItem(rs.getString("username"));
            }

            conn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred while fetching students.");
            ex.printStackTrace();
        }
    }

    private void createTask() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234");

            String description = descriptionField.getText();
            Date startDate = (Date) startDateSpinner.getValue();
            Date startTime = (Date) startTimeSpinner.getValue();
            Date endDate = (Date) endDateSpinner.getValue();
            Date endTime = (Date) endTimeSpinner.getValue();
            String studentName = (String) studentComboBox.getSelectedItem();

            // Combine start date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

            // Combine the start date and start time
            String startDateTimeString = dateFormat.format(startDate) + " " + timeFormat.format(startTime);
            String endDateTimeString = dateFormat.format(endDate) + " " + timeFormat.format(endTime);

            // Find student_id based on the selected student's name
            String studentQuery = "SELECT id FROM users WHERE username = ?";
            PreparedStatement studentStmt = conn.prepareStatement(studentQuery);
            studentStmt.setString(1, studentName);
            ResultSet studentRs = studentStmt.executeQuery();
            int studentId = -1;
            if (studentRs.next()) {
                studentId = studentRs.getInt("id");
            }

            // Find teacher_id based on the teacher's name
            String teacherQuery = "SELECT id FROM users WHERE username = ?";
            PreparedStatement teacherStmt = conn.prepareStatement(teacherQuery);
            teacherStmt.setString(1, teacherName);
            ResultSet teacherRs = teacherStmt.executeQuery();
            int teacherId = -1;
            if (teacherRs.next()) {
                teacherId = teacherRs.getInt("id");
            }

            // Insert task into the database
            String taskQuery = "INSERT INTO task (student_id, teacher_id, description, start_time, end_time) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement taskStmt = conn.prepareStatement(taskQuery);
            taskStmt.setInt(1, studentId);
            taskStmt.setInt(2, teacherId);
            taskStmt.setString(3, description);
            taskStmt.setString(4, startDateTimeString);
            taskStmt.setString(5, endDateTimeString);
            taskStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Task created successfully.");
            dispose();

            // Refresh the teacher's dashboard tasks
            ((TeacherDashboard) getParent()).fetchAndDisplayTasks();

            conn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred while creating the task.");
            ex.printStackTrace();
        }
    }
}




// package gui;

// import javax.swing.*;
// import javax.swing.table.DefaultTableModel;
// import java.sql.*;

// public class TeacherDashboard extends JFrame {
//     private String teacherName;
//     private int teacherId;
//     private DefaultTableModel tableModel;

//     public TeacherDashboard(String name) {
//         this.teacherName = name;
//         setTitle("Teacher Dashboard");
//         setSize(600, 400);
//         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//         setLayout(null);

//         JLabel welcomeLabel = new JLabel("Hello, " + name);
//         welcomeLabel.setBounds(100, 20, 200, 25);
//         add(welcomeLabel);

//         JButton logoutButton = new JButton("Logout");
//         logoutButton.setBounds(100, 300, 100, 30);
//         add(logoutButton);

//         // Table setup
//         tableModel = new DefaultTableModel(new String[]{"Task Description", "Student Name", "Start Time", "End Time", "Status"}, 0);
//         JTable taskTable = new JTable(tableModel);
//         JScrollPane scrollPane = new JScrollPane(taskTable);
//         scrollPane.setBounds(20, 60, 550, 200);
//         add(scrollPane);

//         logoutButton.addActionListener(e -> {
//             dispose();
//             new LoginPage();
//         });

//         // Fetch and display tasks for the teacher
//         fetchAndDisplayTasks();

//         setVisible(true);
//     }

//     private void fetchAndDisplayTasks() {
//         try {
//             Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234");

//             // Retrieve teacher_id from the database
//             String query = "SELECT id FROM users WHERE username = ?";
//             PreparedStatement stmt = conn.prepareStatement(query);
//             stmt.setString(1, teacherName);
//             ResultSet rs = stmt.executeQuery();

//             if (rs.next()) {
//                 teacherId = rs.getInt("id");
//             } else {
//                 JOptionPane.showMessageDialog(this, "Teacher ID not found.");
//                 conn.close();
//                 return;
//             }

//             // Retrieve tasks assigned to this teacher
//             String taskQuery = "SELECT t.description, s.username AS student, t.start_time, t.end_time, t.status " +
//                                 "FROM task t " +
//                                 "JOIN users s ON t.student_id = s.id " +
//                                 "WHERE t.teacher_id = ?";
//             PreparedStatement taskStmt = conn.prepareStatement(taskQuery);
//             taskStmt.setInt(1, teacherId);
//             ResultSet taskRs = taskStmt.executeQuery();

//             tableModel.setRowCount(0); // Clear any existing rows

//             while (taskRs.next()) {
//                 String description = taskRs.getString("description");
//                 String student = taskRs.getString("student");
//                 Timestamp startTime = taskRs.getTimestamp("start_time");
//                 Timestamp endTime = taskRs.getTimestamp("end_time");
//                 String task = taskRs.getString("status");
                


//                 tableModel.addRow(new Object[]{description, student, startTime, endTime, task});
//             }

//             if (tableModel.getRowCount() == 0) {
//                 JOptionPane.showMessageDialog(this, "No tasks found for this teacher.");
//             }

//             tableModel.fireTableDataChanged(); // Notify table model that data has changed

//             conn.close();
//         } catch (SQLException ex) {
//             JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage());
//             ex.printStackTrace();
//         }
//     }

//     public static void main(String[] args) {
//         new TeacherDashboard("Test Teacher");
//     }
// }
