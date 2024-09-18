package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class StudentDashboard extends JFrame {
    private String studentName;
    private int studentId;
    private DefaultTableModel tableModel;

    public StudentDashboard(String name) {
        this.studentName = name;
        setTitle("Student Dashboard");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel welcomeLabel = new JLabel("Hello, " + name);
        welcomeLabel.setBounds(100, 20, 200, 25);
        add(welcomeLabel);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBounds(100, 300, 100, 30);
        add(logoutButton);

        JButton createTaskButton = new JButton("Create Task");
        createTaskButton.setBounds(220, 300, 120, 30);
        add(createTaskButton);

        // Table setup
        tableModel = new DefaultTableModel(new String[]{"Description", "Teacher", "Start Time", "End Time"}, 0);
        JTable taskTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setBounds(20, 60, 550, 200);
        add(scrollPane);

        logoutButton.addActionListener(e -> {
            dispose();
            new LoginPage();
        });

        createTaskButton.addActionListener(e -> openTaskCreationDialog());

        fetchAndDisplayTasks();

        setVisible(true);
    }

    private void openTaskCreationDialog() {
        new TaskCreationDialog(this, studentName);
    }

    private void fetchAndDisplayTasks() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234");

            // Retrieve student_id
            String query = "SELECT id FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, studentName);
            ResultSet rs = stmt.executeQuery();
            // studentId = 3;
            System.out.println("NAME:"+studentName);
            System.out.println("Student ID: " + rs.getInt("id")); // Debug output

            // if (rs.next()) {
            //     studentId = rs.getInt("id");
            //     System.out.println("Student ID: " + studentId); // Debug output
            // } else {
            //     JOptionPane.showMessageDialog(this, "Student ID not found.");
            //     conn.close();
            //     return;
            // }

            // Retrieve tasks
            String taskQuery = "SELECT t.description, u.username AS teacher, t.start_time, t.end_time " +
                                "FROM task t JOIN users u ON t.teacher_id = u.id " +
                                "WHERE t.student_id = ?";
            PreparedStatement taskStmt = conn.prepareStatement(taskQuery);
            taskStmt.setInt(1, studentId);
            ResultSet taskRs = taskStmt.executeQuery();

            tableModel.setRowCount(0); // Clear existing rows
            while (taskRs.next()) {
                String description = taskRs.getString("description");
                String teacher = taskRs.getString("teacher");
                Timestamp startTime = taskRs.getTimestamp("start_time");
                Timestamp endTime = taskRs.getTimestamp("end_time");

                tableModel.addRow(new Object[]{description, teacher, startTime, endTime});
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No tasks found for this student.");
            }

            tableModel.fireTableDataChanged(); // Notify table model that data has changed

            conn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new StudentDashboard("Test Student");
    }
}
