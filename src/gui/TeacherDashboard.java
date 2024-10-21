package gui;

import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Properties;

public class TeacherDashboard extends JFrame {
    private String teacherName;
    private int teacherId;
    private DefaultTableModel tableModel;
    private JTable taskTable;

    public TeacherDashboard(String name) {
        this.teacherName = name;
        setTitle("Teacher Dashboard");
        setSize(800, 400);
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

        // Table setup with columns, including Email button
        tableModel = new DefaultTableModel(new String[]{"Task ID", "Description", "Student Name", "Start Time", "End Time", "Status", "Points", "Edit", "Delete", "Email"}, 0);
        taskTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only allow editing for Edit, Delete, and Email buttons
                return column == 7 || column == 8 || column == 9;
            }

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                if (column == 7 || column == 8 || column == 9) {
                    return new ButtonRenderer();
                }
                return super.getCellRenderer(row, column);
            }
        };

        taskTable.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(new JCheckBox(), "Edit", this));
        taskTable.getColumnModel().getColumn(8).setCellEditor(new ButtonEditor(new JCheckBox(), "Delete", this));
        taskTable.getColumnModel().getColumn(9).setCellEditor(new ButtonEditor(new JCheckBox(), "Email", this)); // Add Email button

        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setBounds(30, 60, 900, 300);
        add(scrollPane);

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

    private void openTaskCreationDialog() {
        new TaskCreationDialogTeacher(this, teacherName); // Pass teacherName to the dialog
    }

    public void fetchAndDisplayTasks() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234")) {
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

            String taskQuery = "SELECT t.id, t.description, s.username AS student, t.start_time, t.end_time, t.status, t.points " +
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
                    int points = taskRs.getInt("points");

                    // Add row to table model with Edit, Delete, and Email buttons
                    tableModel.addRow(new Object[]{taskId, description, student, startTime, endTime, status, points, "Edit", "Delete", "Email"});
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

    public void editTask(int taskId) {
        // Present the user with status options
        String[] statusOptions = {"ToDo", "In Progress", "Done"};
        String newStatus = (String) JOptionPane.showInputDialog(this, "Select new status:", "Update Status",
                JOptionPane.QUESTION_MESSAGE, null, statusOptions, statusOptions[0]);

        if (newStatus != null) {
            // Prompt the user for points input
            String pointsInput = JOptionPane.showInputDialog(this, "Enter points for this task:");
            int points = 0; // Default points if input is invalid

            try {
                points = Integer.parseInt(pointsInput); // Try parsing points
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid points entered. Defaulting to 0 points.");
            }

            // Now, update both the status and the points in the database
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234")) {
                String updateQuery = "UPDATE task SET status = ?, points = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                    stmt.setString(1, newStatus);  // Set the new status
                    stmt.setInt(2, points);        // Set the points
                    stmt.setInt(3, taskId);        // Specify the task to be updated
                    stmt.executeUpdate();
                }
                fetchAndDisplayTasks(); // Refresh the task table after editing
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public void deleteTask(int taskId) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this task?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234")) {
                String deleteQuery = "DELETE FROM task WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                    stmt.setInt(1, taskId);
                    stmt.executeUpdate();
                }
                fetchAndDisplayTasks(); // Refresh the task table after deletion
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public void sendEmailReminder(int taskId) {
        // Fetch email of the student associated with the task
        int userId = fetchUserIdFromTaskId(taskId); // Get the user's ID based on the task ID
        String userEmail = fetchUserEmail(userId, "student"); // Assuming it's a student

        if (userEmail == null) {
            JOptionPane.showMessageDialog(this, "User email not found.");
            return;
        }

        // Set up email properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com"); // Replace with your SMTP server
        props.put("mail.smtp.port", "587"); // Change if needed

        // Authenticate and send email
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("studenttaskmanager1234@gmail.com", "Mohan@9922"); // Replace with your email credentials
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("studenttaskmanager1234@gmail.com")); // Replace with your email
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));
            message.setSubject("Task Reminder");
            message.setText("Dear User,\n\nThis is a reminder that your task is overdue. Please complete it as soon as possible.");

            Transport.send(message);
            JOptionPane.showMessageDialog(this, "Email sent successfully to " + userEmail);
        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(this, "Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Fetch email based on user ID and type (student or teacher)
    private String fetchUserEmail(int userId, String userType) {
        String email = null;
        String query = "SELECT email FROM users WHERE id = ? AND role = ?"; // Assuming user_type is a column in your users table

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234")) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setString(2, userType); // Use "student" or "teacher"
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    email = rs.getString("email");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
        return email;
    }

    // Fetch user ID based on task ID
    private int fetchUserIdFromTaskId(int taskId) {
        int userId = -1; // Default value if not found
        String query = "SELECT student_id FROM task WHERE id = ?"; // Assuming the student_id is stored in the task table

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234")) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, taskId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("student_id");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
        return userId;
    }

    // Custom Button Renderer
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // Custom Button Editor
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private TeacherDashboard dashboard;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox, String label, TeacherDashboard dashboard) {
            super(checkBox);
            this.label = label;
            this.dashboard = dashboard;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    int row = taskTable.getSelectedRow();
                    int taskId = (int) taskTable.getValueAt(row, 0);
                    if ("Edit".equals(label)) {
                        dashboard.editTask(taskId);
                    } else if ("Delete".equals(label)) {
                        dashboard.deleteTask(taskId);
                    } else if ("Email".equals(label)) {
                        dashboard.sendEmailReminder(taskId); // Call the email function
                    }
                }
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            isPushed = true;
            button.setText(label);
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                // Perform action based on button pushed
            }
            isPushed = false;
            return label;
        }
    }

    public static void main(String[] args) {
        new TeacherDashboard("teacher_username"); // Replace with actual teacher username
    }
}








































































// package gui;

// import javax.swing.*;
// import javax.swing.table.DefaultTableModel;
// import javax.swing.table.TableCellRenderer;
// import java.awt.*;
// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
// import java.sql.*;

// public class TeacherDashboard extends JFrame {
//     private String teacherName;
//     private int teacherId;
//     private DefaultTableModel tableModel;
//     private JTable taskTable;

//     public TeacherDashboard(String name) {
//         this.teacherName = name;
//         setTitle("Teacher Dashboard");
//         setSize(800, 400);
//         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//         setLayout(null);

//         // Welcome label
//         JLabel welcomeLabel = new JLabel("Hello, " + name);
//         welcomeLabel.setBounds(100, 20, 200, 25);
//         add(welcomeLabel);

//         // Logout button
//         JButton logoutButton = new JButton("Logout");
//         logoutButton.setBounds(100, 300, 100, 30);
//         add(logoutButton);

//         // Create Task button
//         JButton createTaskButton = new JButton("Create Task");
//         createTaskButton.setBounds(220, 300, 120, 30);
//         add(createTaskButton);

//         // Table setup with columns, including Status, Points, Edit, Delete
//         tableModel = new DefaultTableModel(new String[]{"Task ID", "Description", "Student Name", "Start Time", "End Time", "Status", "Points", "Edit", "Delete"}, 0);
//         taskTable = new JTable(tableModel) {
//             @Override
//             public boolean isCellEditable(int row, int column) {
//                 // Only allow editing for Edit and Delete buttons
//                 return column == 7 || column == 8;
//             }

//             @Override
//             public TableCellRenderer getCellRenderer(int row, int column) {
//                 if (column == 7 || column == 8) {
//                     return new ButtonRenderer();
//                 }
//                 return super.getCellRenderer(row, column);
//             }
//         };

//         taskTable.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(new JCheckBox(), "Edit", this));
//         taskTable.getColumnModel().getColumn(8).setCellEditor(new ButtonEditor(new JCheckBox(), "Delete", this));

//         JScrollPane scrollPane = new JScrollPane(taskTable);
//         scrollPane.setBounds(30, 60, 720, 225);
//         add(scrollPane);

//         // Action listener for logout button
//         logoutButton.addActionListener(e -> {
//             dispose(); // Close current window
//             new LoginPage(); // Open login page
//         });

//         // Action listener for create task button
//         createTaskButton.addActionListener(e -> openTaskCreationDialog());

//         // Fetch and display tasks for the teacher
//         fetchAndDisplayTasks();

//         setVisible(true); // Make frame visible
//     }

//     private void openTaskCreationDialog() {
//         new TaskCreationDialogTeacher(this, teacherName); // Pass teacherName to the dialog
//     }

//     public void fetchAndDisplayTasks() {
//         try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234")) {
//             String query = "SELECT id FROM users WHERE username = ?";
//             try (PreparedStatement stmt = conn.prepareStatement(query)) {
//                 stmt.setString(1, teacherName);
//                 ResultSet rs = stmt.executeQuery();
//                 if (rs.next()) {
//                     teacherId = rs.getInt("id");
//                 } else {
//                     JOptionPane.showMessageDialog(this, "Teacher ID not found.");
//                     return;
//                 }
//             }

//             String taskQuery = "SELECT t.id, t.description, s.username AS student, t.start_time, t.end_time, t.status, t.points " +
//                                "FROM task t " +
//                                "JOIN users s ON t.student_id = s.id " +
//                                "WHERE t.teacher_id = ?";
//             try (PreparedStatement taskStmt = conn.prepareStatement(taskQuery)) {
//                 taskStmt.setInt(1, teacherId);
//                 ResultSet taskRs = taskStmt.executeQuery();

//                 tableModel.setRowCount(0); // Clear any existing rows
//                 while (taskRs.next()) {
//                     int taskId = taskRs.getInt("id");
//                     String description = taskRs.getString("description");
//                     String student = taskRs.getString("student");
//                     Timestamp startTime = taskRs.getTimestamp("start_time");
//                     Timestamp endTime = taskRs.getTimestamp("end_time");
//                     String status = taskRs.getString("status");
//                     int points = taskRs.getInt("points");

//                     // Add row to table model with Edit and Delete buttons
//                     tableModel.addRow(new Object[]{taskId, description, student, startTime, endTime, status, points, "Edit", "Delete"});
//                 }

//                 if (tableModel.getRowCount() == 0) {
//                     JOptionPane.showMessageDialog(this, "No tasks found for this teacher.");
//                 }

//                 tableModel.fireTableDataChanged(); // Notify table model that data has changed
//             }
//         } catch (SQLException ex) {
//             JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage());
//             ex.printStackTrace();
//         }
//     }

//     // Edit task functionality
//     // public void editTask(int taskId) {
//     //     // Present the user with status options
//     //     String[] statusOptions = {"ToDo", "In Progress", "Done"};
//     //     String newStatus = (String) JOptionPane.showInputDialog(this, "Select new status:", "Update Status",
//     //             JOptionPane.QUESTION_MESSAGE, null, statusOptions, statusOptions[0]);

//     //     if (newStatus != null) {
//     //         // Ask for points input
//     //         String pointsInput = JOptionPane.showInputDialog(this, "Enter points for this task:");
//     //         int newPoints = 0;
//     //         try {
//     //             newPoints = Integer.parseInt(pointsInput);
//     //         } catch (NumberFormatException e) {
//     //             JOptionPane.showMessageDialog(this, "Invalid points input. Points will be set to 0.");
//     //         }

//     //         try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234")) {
//     //             String updateQuery = "UPDATE task SET status = ?, points = ? WHERE id = ?";
//     //             try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
//     //                 stmt.setString(1, newStatus);
//     //                 stmt.setInt(2, newPoints);
//     //                 stmt.setInt(3, taskId);
//     //                 stmt.executeUpdate();
//     //             }
//     //             fetchAndDisplayTasks(); // Refresh the task table after editing
//     //         } catch (SQLException ex) {
//     //             JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage());
//     //             ex.printStackTrace();
//     //         }
//     //     }
//     // }


//     // Edit task functionality
// public void editTask(int taskId) {
//     // Present the user with status options
//     String[] statusOptions = {"ToDo", "In Progress", "Done"};
//     String newStatus = (String) JOptionPane.showInputDialog(this, "Select new status:", "Update Status",
//             JOptionPane.QUESTION_MESSAGE, null, statusOptions, statusOptions[0]);

//     if (newStatus != null) {
//         // Prompt the user for points input
//         String pointsInput = JOptionPane.showInputDialog(this, "Enter points for this task:");
//         int points = 0; // Default points if input is invalid

//         try {
//             points = Integer.parseInt(pointsInput); // Try parsing points
//         } catch (NumberFormatException e) {
//             JOptionPane.showMessageDialog(this, "Invalid points entered. Defaulting to 0 points.");
//         }

//         // Now, update both the status and the points in the database
//         try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234")) {
//             String updateQuery = "UPDATE task SET status = ?, points = ? WHERE id = ?";
//             try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
//                 stmt.setString(1, newStatus);  // Set the new status
//                 stmt.setInt(2, points);        // Set the points
//                 stmt.setInt(3, taskId);        // Specify the task to be updated
//                 stmt.executeUpdate();
//             }
//             fetchAndDisplayTasks(); // Refresh the task table after editing
//         } catch (SQLException ex) {
//             JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage());
//             ex.printStackTrace();
//         }
//     }
// }


//     // Delete task functionality
//     public void deleteTask(int taskId) {
//         int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this task?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
//         if (confirm == JOptionPane.YES_OPTION) {
//             try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234")) {
//                 String deleteQuery = "DELETE FROM task WHERE id = ?";
//                 try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
//                     stmt.setInt(1, taskId);
//                     stmt.executeUpdate();
//                 }
//                 fetchAndDisplayTasks(); // Refresh the task table after deletion
//             } catch (SQLException ex) {
//                 JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage());
//                 ex.printStackTrace();
//             }
//         }
//     }

//     public static void main(String[] args) {
//         TeacherDashboard dashboard = new TeacherDashboard("TeacherName"); // Pass teacher name for testing
//     }
// }

// // Button renderer to show Edit and Delete buttons
// class ButtonRenderer extends JButton implements TableCellRenderer {
//     public ButtonRenderer() {
//         setOpaque(true);
//     }

//     public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//         setText((value == null) ? "" : value.toString());
//         return this;
//     }
// }

// // Button editor to handle Edit and Delete button clicks
// class ButtonEditor extends DefaultCellEditor {
//     private String label;
//     private TeacherDashboard dashboard;
//     private boolean isPushed;

//     public ButtonEditor(JCheckBox checkBox, String buttonLabel, TeacherDashboard dashboard) {
//         super(checkBox);
//         this.dashboard = dashboard;
//         this.label = buttonLabel;
//     }

//     public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//         label = (value == null) ? "" : value.toString();
//         isPushed = true;
//         JButton button = new JButton(label);
//         button.addActionListener(new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 if ("Edit".equals(label)) {
//                     int taskId = (int) table.getValueAt(row, 0);
//                     dashboard.editTask(taskId);
//                 } else if ("Delete".equals(label)) {
//                     int taskId = (int) table.getValueAt(row, 0);
//                     dashboard.deleteTask(taskId);
//                 }
//             }
//         });
//         return button;
//     }

//     public Object getCellEditorValue() {
//         isPushed = false;
//         return label;
//     }

//     public boolean stopCellEditing() {
//         isPushed = false;
//         return super.stopCellEditing();
//     }

//     protected void fireEditingStopped() {
//         super.fireEditingStopped();
//     }
// }
