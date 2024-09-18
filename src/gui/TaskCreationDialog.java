package gui;

import javax.swing.*;
import java.awt.event.*;
import java.sql.*;

public class TaskCreationDialog extends JDialog {
    private JTextField taskDescriptionField;
    private JComboBox<String> teacherComboBox;
    private JSpinner startDateSpinner, endDateSpinner;
    private JButton saveButton;

    public TaskCreationDialog(JFrame parent, String studentName) {
        super(parent, "Create Task", true);

        setSize(400, 300);
        setLayout(null);
        setLocationRelativeTo(parent);

        JLabel descriptionLabel = new JLabel("Task Description:");
        descriptionLabel.setBounds(20, 20, 150, 25);
        add(descriptionLabel);

        taskDescriptionField = new JTextField();
        taskDescriptionField.setBounds(180, 20, 180, 25);
        add(taskDescriptionField);

        JLabel teacherLabel = new JLabel("Select Teacher:");
        teacherLabel.setBounds(20, 60, 150, 25);
        add(teacherLabel);

        teacherComboBox = new JComboBox<>();
        teacherComboBox.setBounds(180, 60, 180, 25);
        add(teacherComboBox);
        populateTeacherComboBox();

        JLabel startDateLabel = new JLabel("Start Date:");
        startDateLabel.setBounds(20, 100, 150, 25);
        add(startDateLabel);

        startDateSpinner = new JSpinner(new SpinnerDateModel());
        startDateSpinner.setBounds(180, 100, 180, 25);
        add(startDateSpinner);

        JLabel endDateLabel = new JLabel("End Date:");
        endDateLabel.setBounds(20, 140, 150, 25);
        add(endDateLabel);

        endDateSpinner = new JSpinner(new SpinnerDateModel());
        endDateSpinner.setBounds(180, 140, 180, 25);
        add(endDateSpinner);

        saveButton = new JButton("Save Task");
        saveButton.setBounds(150, 200, 120, 30);
        add(saveButton);

        saveButton.addActionListener(e -> saveTask());

        setVisible(true);
    }

    private void populateTeacherComboBox() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234");
            String query = "SELECT username FROM users WHERE role = 'teacher'";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                teacherComboBox.addItem(rs.getString("username"));
            }
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void saveTask() {
        String taskDescription = taskDescriptionField.getText();
        String teacherUsername = (String) teacherComboBox.getSelectedItem();
        java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
        java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234");

            // Get the student ID from the static variable
            int studentId = LoginPage.loggedInStudentId;

            // Get the teacher ID
            String teacherQuery = "SELECT id FROM users WHERE username = ?";
            PreparedStatement teacherStmt = conn.prepareStatement(teacherQuery);
            teacherStmt.setString(1, teacherUsername);
            ResultSet teacherRs = teacherStmt.executeQuery();
            int teacherId = -1;
            if (teacherRs.next()) {
                teacherId = teacherRs.getInt("id");
            }

            if (studentId == -1 || teacherId == -1) {
                JOptionPane.showMessageDialog(this, "Error: Could not find student or teacher.");
                return;
            }

            // Insert the task into the database
            String insertTaskQuery = "INSERT INTO task (student_id, teacher_id, description, start_time, end_time) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement taskStmt = conn.prepareStatement(insertTaskQuery);
            taskStmt.setInt(1, studentId);
            taskStmt.setInt(2, teacherId);
            taskStmt.setString(3, taskDescription);
            taskStmt.setTimestamp(4, new java.sql.Timestamp(startDate.getTime()));
            taskStmt.setTimestamp(5, new java.sql.Timestamp(endDate.getTime()));

            taskStmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Task Created Successfully!");
            dispose();
            conn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
