package gui;
import javax.swing.*;

class TeacherDashboard extends JFrame {
    public TeacherDashboard(String name) {
        setTitle("Teacher Dashboard");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel welcomeLabel = new JLabel("Hello, " + name);
        welcomeLabel.setBounds(100, 50, 150, 25);
        add(welcomeLabel);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBounds(100, 100, 100, 30);
        add(logoutButton);

        logoutButton.addActionListener(e -> {
            dispose();
            new LoginPage();
        });

        setVisible(true);
    }
}