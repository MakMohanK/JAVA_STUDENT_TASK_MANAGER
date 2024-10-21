package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class StudentDashboard extends JFrame {
    private String studentName; // Store the student's name
    private int studentId; // Store the student's ID
    private DefaultTableModel tableModel; // Table model for the JTable

    public StudentDashboard(String name) {
        this.studentName = name; // Initialize the student's name
        setTitle("Student Dashboard");
        setSize(800, 400); // Adjusted size for better visibility
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); // Using null layout for manual positioning

        // Welcome label
        JLabel welcomeLabel = new JLabel("Hello, " + name);
        welcomeLabel.setBounds(100, 20, 200, 25); // Positioning label
        add(welcomeLabel);

        // Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBounds(100, 300, 100, 30);
        add(logoutButton);

        // Create Task button
        JButton createTaskButton = new JButton("Create Task");
        createTaskButton.setBounds(220, 300, 120, 30);
        add(createTaskButton);

        // Table setup with columns
        tableModel = new DefaultTableModel(new String[]{"Task ID", "Description", "Teacher", "Start Time", "End Time", "Status", "Edit", "Delete"}, 0);
        JTable taskTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setBounds(30, 60, 720, 225); // Adjusted for better fit
        add(scrollPane);

        // Add custom renderer and editor for the "Edit" and "Delete" columns
        taskTable.getColumn("Edit").setCellRenderer(new ButtonRenderer());
        taskTable.getColumn("Edit").setCellEditor(new ButtonEditor(new JCheckBox(), this));
        taskTable.getColumn("Delete").setCellRenderer(new DeleteButtonRenderer());
        taskTable.getColumn("Delete").setCellEditor(new DeleteButtonEditor(new JCheckBox(), this));

        // Action listener for logout button
        logoutButton.addActionListener(e -> {
            dispose(); // Close current window
            new LoginPage(); // Open login page
        });

        // Action listener for create task button
        createTaskButton.addActionListener(e -> openTaskCreationDialog());

        // Fetch and display tasks for the student
        fetchAndDisplayTasks();

        setVisible(true); // Make frame visible
    }

    // Method to open a dialog for task creation
    private void openTaskCreationDialog() {
        new TaskCreationDialog(this, studentName);
    }

    // Method to fetch tasks from the database and display them in the JTable
    public void fetchAndDisplayTasks() {
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement taskStmt = null;
        ResultSet rs = null;
        ResultSet taskRs = null;

        try {
            // Connect to the MySQL database
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234");
            // Query to retrieve student ID
            String query = "SELECT id FROM users WHERE username = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, studentName);
            rs = stmt.executeQuery();

            // Get student ID
            if (rs.next()) {
                studentId = rs.getInt("id");
            } else {
                JOptionPane.showMessageDialog(this, "Student ID not found.");
                return;
            }

            // Retrieve tasks based on student ID
            String taskQuery = "SELECT t.id, t.description, u.username AS teacher, t.start_time, t.end_time, t.status " +
                               "FROM task t JOIN users u ON t.teacher_id = u.id " +
                               "WHERE t.student_id = ?";
            taskStmt = conn.prepareStatement(taskQuery);
            taskStmt.setInt(1, studentId);
            taskRs = taskStmt.executeQuery();

            // Clear existing rows in table model
            tableModel.setRowCount(0);

            // Loop through result set and add tasks to table
            while (taskRs.next()) {
                int taskId = taskRs.getInt("id");
                String description = taskRs.getString("description");
                String teacher = taskRs.getString("teacher");
                Timestamp startTime = taskRs.getTimestamp("start_time");
                Timestamp endTime = taskRs.getTimestamp("end_time");
                String status = taskRs.getString("status");

                // Add row to table model
                tableModel.addRow(new Object[]{taskId, description, teacher, startTime, endTime, status, "Edit", "Delete"});
            }

            // Handle case where no tasks are found
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No tasks found for this student.");
            }

            // Notify table model that data has changed
            tableModel.fireTableDataChanged();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            // Clean up resources
            try {
                if (rs != null) rs.close();
                if (taskRs != null) taskRs.close();
                if (stmt != null) stmt.close();
                if (taskStmt != null) taskStmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Method to update task status in the database
    public void updateTaskStatus(int taskId, String newStatus) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234");
            String updateQuery = "UPDATE task SET status = ? WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setString(1, newStatus);
            updateStmt.setInt(2, taskId);
            int rowsAffected = updateStmt.executeUpdate();

            // Show success or failure message
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Status updated successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update status.");
            }

            updateStmt.close();
            conn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred while updating the status: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Method to delete task from the database
    public void deleteTask(int taskId) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234");
            String deleteQuery = "DELETE FROM task WHERE id = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
            deleteStmt.setInt(1, taskId);
            int rowsAffected = deleteStmt.executeUpdate();

            // Show success or failure message
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Task deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete task.");
            }

            deleteStmt.close();
            conn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred while deleting the task: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Custom button renderer for JTable
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true); // Make button opaque
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Edit" : value.toString()); // Set button text
            return this; // Return button
        }
    }

    // Custom button editor for JTable (Edit button)
    class ButtonEditor extends DefaultCellEditor {
        private JButton button; // Button for editing
        private String label; // Button label
        private boolean isPushed; // Track if button is pushed
        private int selectedRow; // Track selected row
        private StudentDashboard parent; // Reference to the parent dashboard

        public ButtonEditor(JCheckBox checkBox, StudentDashboard parent) {
            super(checkBox);
            this.parent = parent; // Initialize parent reference
            button = new JButton(); // Create button
            button.setOpaque(true); // Make button opaque
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped(); // Stop editing when button is pressed
                }
            });
        }

        // Method to get the button component for editing
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            selectedRow = row; // Store the selected row
            label = (value == null) ? "Edit" : value.toString(); // Set button label
            button.setText(label);
            isPushed = true; // Mark as pushed
            return button; // Return button component
        }

        // Method to get the cell editor value after editing
        public Object getCellEditorValue() {
            if (isPushed) {
                // Get the current status (allow blank)
                String currentStatus = tableModel.getValueAt(selectedRow, 5) != null ? tableModel.getValueAt(selectedRow, 5).toString() : "";
                String[] statuses = {"Pending", "In Progress", "Completed"};
                String newStatus = (String) JOptionPane.showInputDialog(null, "Update Status", "Edit Status",
                        JOptionPane.QUESTION_MESSAGE, null, statuses, currentStatus);

                // Update status if newStatus is not null
                if (newStatus != null) {
                    tableModel.setValueAt(newStatus, selectedRow, 5); // Update the table model
                    int taskId = (int) tableModel.getValueAt(selectedRow, 0); // Get task ID
                    parent.updateTaskStatus(taskId, newStatus); // Update status in database
                }
            }
            isPushed = false; // Reset push flag
            return label; // Return button label
        }

        public boolean stopCellEditing() {
            isPushed = false; // Reset push flag
            return super.stopCellEditing(); // Stop cell editing
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped(); // Fire editing stopped event
        }
    }

    // Custom button editor for "Delete" column
    class DeleteButtonEditor extends DefaultCellEditor {
        private JButton button; // Button for deleting
        private String label; // Button label
        private boolean isPushed; // Track if button is pushed
        private int selectedRow; // Track selected row
        private StudentDashboard parent; // Reference to the parent dashboard

        public DeleteButtonEditor(JCheckBox checkBox, StudentDashboard parent) {
            super(checkBox);
            this.parent = parent; // Initialize parent reference
            button = new JButton(); // Create button
            button.setOpaque(true); // Make button opaque
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped(); // Stop editing when button is pressed
                }
            });
        }

        // Method to get the button component for editing
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            selectedRow = row; // Store the selected row
            label = (value == null) ? "Delete" : value.toString(); // Set button label
            button.setText(label);
            isPushed = true; // Mark as pushed
            return button; // Return button component
        }

        // Method to get the cell editor value after editing
        public Object getCellEditorValue() {
            if (isPushed) {
                // Confirm deletion
                int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this task?", "Delete Task", JOptionPane.YES_NO_OPTION);
                if (confirmation == JOptionPane.YES_OPTION) {
                    // Get the task ID from the first column
                    int taskId = (int) tableModel.getValueAt(selectedRow, 0);
                    // Call method to delete the task from the database
                    parent.deleteTask(taskId);
                    // Remove the row from the table
                    tableModel.removeRow(selectedRow);
                }
            }
            isPushed = false; // Reset push flag
            return label; // Return button label
        }

        public boolean stopCellEditing() {
            isPushed = false; // Reset push flag
            return super.stopCellEditing(); // Stop cell editing
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped(); // Fire editing stopped event
        }
    }

    // Custom button renderer for "Delete" column
    class DeleteButtonRenderer extends JButton implements TableCellRenderer {
        public DeleteButtonRenderer() {
            setOpaque(true); // Make button opaque
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Delete" : value.toString()); // Set button text
            return this; // Return button
        }
    }

    // Main method to run the application
    public static void main(String[] args) {
        new StudentDashboard("student_name"); // Replace with actual student name
    }
}
