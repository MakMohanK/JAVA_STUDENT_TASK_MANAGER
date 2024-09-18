package gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskCreationDialog extends JDialog {
    private JTextField descriptionField;
    private JSpinner startDateSpinner, startTimeSpinner;
    private JSpinner endDateSpinner, endTimeSpinner;
    private JComboBox<String> teacherComboBox;
    private JButton createButton, cancelButton;
    private String studentName;

    public TaskCreationDialog(JFrame parent, String studentName) {
        super(parent, "Create Task", true);
        this.studentName = studentName;

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

        // Teacher selection dropdown
        JLabel teacherLabel = new JLabel("Assign Teacher:");
        teacherLabel.setBounds(20, 220, 150, 25);
        add(teacherLabel);

        teacherComboBox = new JComboBox<>();
        teacherComboBox.setBounds(180, 220, 150, 25);
        add(teacherComboBox);
        populateTeacherComboBox();

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
            JOptionPane.showMessageDialog(this, "An error occurred while fetching teachers.");
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
            String teacherName = (String) teacherComboBox.getSelectedItem();

            // Combine start date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

            // Combine the start date and start time
            String startDateTimeString = dateFormat.format(startDate) + " " + timeFormat.format(startTime);
            String endDateTimeString = dateFormat.format(endDate) + " " + timeFormat.format(endTime);

            // Find teacher_id based on the selected teacher's name
            String teacherQuery = "SELECT id FROM users WHERE username = ?";
            PreparedStatement teacherStmt = conn.prepareStatement(teacherQuery);
            teacherStmt.setString(1, teacherName);
            ResultSet rs = teacherStmt.executeQuery();
            int teacherId = -1;
            if (rs.next()) {
                teacherId = rs.getInt("id");
            }

            // Find student_id
            String studentQuery = "SELECT id FROM users WHERE username = ?";
            PreparedStatement studentStmt = conn.prepareStatement(studentQuery);
            studentStmt.setString(1, studentName);
            ResultSet studentRs = studentStmt.executeQuery();
            int studentId = -1;
            if (studentRs.next()) {
                studentId = studentRs.getInt("id");
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

            // Refresh the dashboard tasks
            ((StudentDashboard) getParent()).fetchAndDisplayTasks();

            conn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred while creating the task.");
            ex.printStackTrace();
        }
    }
}
