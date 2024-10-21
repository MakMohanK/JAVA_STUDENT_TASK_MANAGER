package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.sql.*;

public class TeacherDashboard extends JFrame {
    private String teacherName;
    private int teacherId;
    private DefaultTableModel tableModel;

    public TeacherDashboard(String name) {
        this.teacherName = name;
        setTitle("Teacher Dashboard");
        setSize(800, 400); // Adjusted size for better visibility
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        // Welcome label
        JLabel welcomeLabel = new JLabel("Hello, " + name);
        welcomeLabel.setBounds(100, 20, 200, 25);
        add(welcomeLabel);

        // Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBounds(100, 300, 100, 30);
        add(logoutButton);

        // Create Task button
        JButton createTaskButton = new JButton("Create Task");
        createTaskButton.setBounds(220, 300, 120, 30);
        add(createTaskButton);

        // Table setup with columns (removed "Edit" column)
        tableModel = new DefaultTableModel(new String[]{"Task ID", "Description", "Student Name", "Start Time", "End Time", "Status", "Edit", "Delete"}, 0);
        JTable taskTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setBounds(30, 60, 720, 225); // Adjusted for better fit
        add(scrollPane);

        // Add custom renderer and editor for the "Update Status" and "Delete" columns
        taskTable.getColumn("Edit").setCellRenderer(new StatusButtonRenderer());
        taskTable.getColumn("Edit").setCellEditor(new StatusButtonEditor(new JCheckBox(), this));
        taskTable.getColumn("Delete").setCellRenderer(new DeleteButtonRenderer());
        taskTable.getColumn("Delete").setCellEditor(new DeleteButtonEditor(new JCheckBox(), this, taskTable));

        // Action listener for logout button
        logoutButton.addActionListener(e -> {
            dispose(); // Close current window
            new LoginPage(); // Open login page
        });

        // Action listener for create task button
        createTaskButton.addActionListener(e -> openTaskCreationDialog());

        // Fetch and display tasks for the teacher
        fetchAndDisplayTasks();

        setVisible(true); // Make frame visible
    }

    // Method to open a dialog for task creation
    private void openTaskCreationDialog() {
        new TaskCreationDialog(this, String.valueOf(teacherId)); // Ensure TaskCreationDialog is implemented correctly
    }

    // Method to fetch tasks from the database and display them in the JTable
    private void fetchAndDisplayTasks() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234")) {
            // Query to retrieve teacher_id
            String query = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, teacherName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    teacherId = rs.getInt("id");
                } else {
                    JOptionPane.showMessageDialog(this, "Teacher ID not found.");
                    return;
                }
            }

            // Retrieve tasks assigned to this teacher
            String taskQuery = "SELECT t.id, t.description, s.username AS student, t.start_time, t.end_time, t.status " +
                               "FROM task t " +
                               "JOIN users s ON t.student_id = s.id " +
                               "WHERE t.teacher_id = ?";
            try (PreparedStatement taskStmt = conn.prepareStatement(taskQuery)) {
                taskStmt.setInt(1, teacherId);
                ResultSet taskRs = taskStmt.executeQuery();

                tableModel.setRowCount(0); // Clear any existing rows
                while (taskRs.next()) {
                    int taskId = taskRs.getInt("id");
                    String description = taskRs.getString("description");
                    String student = taskRs.getString("student");
                    Timestamp startTime = taskRs.getTimestamp("start_time");
                    Timestamp endTime = taskRs.getTimestamp("end_time");
                    String status = taskRs.getString("status");

                    // Add row to table model
                    tableModel.addRow(new Object[]{taskId, description, student, startTime, endTime, status, "Edite", "Delete"});
                }

                if (tableModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(this, "No tasks found for this teacher.");
                }

                tableModel.fireTableDataChanged(); // Notify table model that data has changed
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Custom button renderer for "Update Status" column
    class StatusButtonRenderer extends JButton implements TableCellRenderer {
        public StatusButtonRenderer() {
            setOpaque(true); // Make button opaque
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Edite"); // Set button text
            return this; // Return button
        }
    }

    // Custom button editor for "Update Status" column
    class StatusButtonEditor extends DefaultCellEditor {
        private JButton button; // Button for updating status
        private boolean isPushed; // Track if button is pushed
        private int selectedRow; // Track selected row
        private TeacherDashboard parent; // Reference to the parent dashboard

        public StatusButtonEditor(JCheckBox checkBox, TeacherDashboard parent) {
            super(checkBox);
            this.parent = parent; // Initialize parent reference
            button = new JButton(); // Create button
            button.setOpaque(true); // Make button opaque
            button.addActionListener(e -> {
                fireEditingStopped(); // Stop editing when button is pressed
                updateTaskStatus(); // Call method to update task status
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            selectedRow = row; // Store the selected row
            button.setText("Edit"); // Set button label
            isPushed = true; // Mark as pushed
            return button; // Return button component
        }

        public Object getCellEditorValue() {
            isPushed = false; // Reset push flag
            return button.getText(); // Return button label
        }

        public boolean stopCellEditing() {
            isPushed = false; // Reset push flag
            return super.stopCellEditing(); // Stop cell editing
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped(); // Fire editing stopped event
        }

        private void updateTaskStatus() {
            int taskId = (int) tableModel.getValueAt(selectedRow, 0); // Get task ID
            String currentStatus = (String) tableModel.getValueAt(selectedRow, 5); // Get current status

            // Show dialog to select new status
            String[] statuses = {"Pending", "In Progress", "Complete"};
            String newStatus = (String) JOptionPane.showInputDialog(button, "Select new status:", "Edit", JOptionPane.QUESTION_MESSAGE, null, statuses, currentStatus);

            if (newStatus != null) {
                // Update the task status in the database
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234")) {
                    String query = "UPDATE task SET status = ? WHERE id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, newStatus);
                        stmt.setInt(2, taskId);
                        stmt.executeUpdate();
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(parent, "An error occurred while updating the status: " + ex.getMessage());
                    ex.printStackTrace();
                }

                // Update the table model to reflect the new status
                tableModel.setValueAt(newStatus, selectedRow, 5);
            }
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

    // Custom button editor for "Delete" column
    class DeleteButtonEditor extends DefaultCellEditor {
        private JButton button; // Button for deleting
        private String label; // Button label
        private boolean isPushed; // Track if button is pushed
        private TeacherDashboard parent; // Reference to the parent dashboard
        private JTable table; // Reference to the table

        public DeleteButtonEditor(JCheckBox checkBox, TeacherDashboard parent, JTable table) {
            super(checkBox);
            this.parent = parent; // Initialize parent reference
            this.table = table; // Initialize table reference
            button = new JButton(); // Create button
            button.setOpaque(true); // Make button opaque
            button.addActionListener(e -> fireEditingStopped()); // Stop editing when button is pressed
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "Delete" : value.toString(); // Set button label
            button.setText(label);
            isPushed = true; // Mark as pushed
            return button; // Return button component
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                // Confirm deletion
                int confirmation = JOptionPane.showConfirmDialog(button, "Are you sure you want to delete this task?", "Delete Task", JOptionPane.YES_NO_OPTION);
                if (confirmation == JOptionPane.YES_OPTION) {
                    int selectedRow = table.getSelectedRow(); // Get selected row using the reference
                    int taskId = (int) tableModel.getValueAt(selectedRow, 0); // Get task ID
                    parent.deleteTask(taskId); // Delete task from database
                    tableModel.removeRow(selectedRow); // Remove row from the table model
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

    // Method to delete a task
    public void deleteTask(int taskId) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234")) {
            String query = "DELETE FROM task WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, taskId);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred while deleting the task: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TeacherDashboard("TeacherName"); // Pass teacher name as argument
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
