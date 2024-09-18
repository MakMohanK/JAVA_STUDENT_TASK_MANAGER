package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseOperations {
    public static void main(String[] args) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                // Create a statement
                Statement stmt = conn.createStatement();
                
                // Execute a query
                String query = "SELECT * FROM users";
                ResultSet rs = stmt.executeQuery(query);

                // Process the result set
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String username = rs.getString("username");
                    System.out.println("ID: " + id + ", Username: " + username);
                }
                
                // Close resources
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
