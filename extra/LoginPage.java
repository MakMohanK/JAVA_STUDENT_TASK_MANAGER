package gui;

import javax.swing.*;
import java.awt.event.*;
import java.sql.*;

public class LoginPage extends JFrame {
    JTextField usernameField;
    JPasswordField passwordField;

    public LoginPage() {
        setTitle("Login Page");
        setSize(300, 250); // Increase the size to fit the new button
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        // Create fields
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(20, 20, 80, 25);
        add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(100, 20, 150, 25);
        add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(20, 60, 80, 25);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(100, 60, 150, 25);
        add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(100, 100, 150, 30);
        add(loginButton);

        JButton signUpButton = new JButton("Sign Up");
        signUpButton.setBounds(100, 140, 150, 30); // Position the Sign Up button below the Login button
        add(signUpButton);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loginUser();
            }
        });

        signUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openSignUpPage();
            }
        });

        setVisible(true);
    }

    public void loginUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management", "root", "mohan@1234");
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            System.out.println(rs);

            if (rs.next()) {
                String role = rs.getString("role");
                String name = rs.getString("name");
                int id = rs.getInt("id");
                JOptionPane.showMessageDialog(null, "Login Successful!");

                if (role.equals("student")) {
                    new StudentDashboard(name, id);
                } else if (role.equals("teacher")) {
                    new TeacherDashboard(name, id);
                }

                dispose(); // Close the login page
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Username or Password");
            }
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void openSignUpPage() {
        setVisible(false); // Make the login page invisible
        new SignUpPage(); // Open the Sign Up page
    }

    public static void main(String[] args) {
        new LoginPage();
    }
}
