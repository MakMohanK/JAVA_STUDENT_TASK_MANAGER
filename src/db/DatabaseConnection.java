package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Database URL, username, and password
    private static final String URL = "jdbc:mysql://localhost:3306/task_management"; // Update with your DB URL
    private static final String USER = "root"; // Update with your DB username
    private static final String PASSWORD = "mohan@1234"; // Update with your DB password

    // Method to get a connection to the database
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Load the MySQL JDBC driver (optional in newer versions)
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establish the connection
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connection established.");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Connection failed.");
            e.printStackTrace();
        }
        return connection;
    }
    
    // Main method for testing the connection
    public static void main(String[] args) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
