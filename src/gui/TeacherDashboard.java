package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class TeacherDashboard extends JFrame {
    private String teacherName;
    private int teacherId;
    private DefaultTableModel tableModel;

    public TeacherDashboard(String name) {
        this.teacherName = name;
        setTitle("Teacher Dashboard");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel welcomeLabel = new JLabel("Hello, " + name);
        welcomeLabel.setBounds(100, 20, 200, 25);
        add(welcomeLabel);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBounds(100, 300, 100, 30);
        add(logoutButton);

        // Table setup
        tableModel = new DefaultTableModel(new String[]{"Task Description", "Student Name", "Start Time", "End Time"}, 0);
        JTable taskTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setBounds(20, 60, 550, 200);
        add(scrollPane);

        logoutButton.addActionListener(e -> {
            dispose();
            new LoginPage();
        });

        // Fetch and display tasks for the teacher
        fetchAndDisplayTasks();

        setVisible(true);
    }

    private void fetchAndDisplayTasks() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234");

            // Retrieve teacher_id from the database
            String query = "SELECT id FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, teacherName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                teacherId = rs.getInt("id");
            } else {
                JOptionPane.showMessageDialog(this, "Teacher ID not found.");
                conn.close();
                return;
            }

            // Retrieve tasks assigned to this teacher
            String taskQuery = "SELECT t.description, s.username AS student, t.start_time, t.end_time " +
                                "FROM task t " +
                                "JOIN users s ON t.student_id = s.id " +
                                "WHERE t.teacher_id = ?";
            PreparedStatement taskStmt = conn.prepareStatement(taskQuery);
            taskStmt.setInt(1, teacherId);
            ResultSet taskRs = taskStmt.executeQuery();

            tableModel.setRowCount(0); // Clear any existing rows

            while (taskRs.next()) {
                String description = taskRs.getString("description");
                String student = taskRs.getString("student");
                Timestamp startTime = taskRs.getTimestamp("start_time");
                Timestamp endTime = taskRs.getTimestamp("end_time");

                tableModel.addRow(new Object[]{description, student, startTime, endTime});
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No tasks found for this teacher.");
            }

            tableModel.fireTableDataChanged(); // Notify table model that data has changed

            conn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TeacherDashboard("Test Teacher");
    }
}
