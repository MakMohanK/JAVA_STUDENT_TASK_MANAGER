package gui;

import javax.swing.*;
import java.awt.event.*;
import java.sql.*;

public class SignUpPage extends JFrame {
    JTextField usernameField, nameField, emailField, contactField;
    JPasswordField passwordField;
    JComboBox<String> roleBox;

    public SignUpPage() {
        setTitle("Sign-Up Page");
        setSize(300, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        // Create fields
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(20, 20, 80, 25);
        add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(100, 20, 150, 25);
        add(usernameField);

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setBounds(20, 60, 80, 25);
        add(nameLabel);

        nameField = new JTextField();
        nameField.setBounds(100, 60, 150, 25);
        add(nameField);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(20, 100, 80, 25);
        add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(100, 100, 150, 25);
        add(emailField);

        JLabel contactLabel = new JLabel("Contact:");
        contactLabel.setBounds(20, 140, 80, 25);
        add(contactLabel);

        contactField = new JTextField();
        contactField.setBounds(100, 140, 150, 25);
        add(contactField);

        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setBounds(20, 180, 80, 25);
        add(roleLabel);

        roleBox = new JComboBox<>(new String[]{"teacher", "student"});
        roleBox.setBounds(100, 180, 150, 25);
        add(roleBox);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(20, 220, 80, 25);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(100, 220, 150, 25);
        add(passwordField);

        JButton submitButton = new JButton("Sign Up");
        submitButton.setBounds(100, 270, 150, 30);
        add(submitButton);

        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                signUpUser();
            }
        });

        setVisible(true);
    }

    public void signUpUser() {
        String username = usernameField.getText();
        String name = nameField.getText();
        String email = emailField.getText();
        String contact = contactField.getText();
        String role = (String) roleBox.getSelectedItem();
        String password = new String(passwordField.getPassword());

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234");
            String query = "INSERT INTO users (username, name, email, contact, role, password) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, name);
            stmt.setString(3, email);
            stmt.setString(4, contact);
            stmt.setString(5, role);
            stmt.setString(6, password);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(null, "User Registered Successfully!");
                dispose(); // Close the sign-up page

                // Open the login page
                new LoginPage();
            } else {
                JOptionPane.showMessageDialog(null, "Registration Failed. Please try again.");
            }
            conn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "An error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new SignUpPage();
    }
}
