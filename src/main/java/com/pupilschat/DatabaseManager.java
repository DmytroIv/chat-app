package com.pupilschat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5432/chatdb";
    private static final String USER = "chatuser";
    private static final String PASSWORD = "chatpassword";

    // connect to the db
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // create a new messages table
    public static void initializeDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS messages ("
                + "id SERIAL PRIMARY KEY, "
                + "sender_address VARCHAR(50), "
                + "message_content TEXT NOT NULL, "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ");";

        // 'try-with-resources' automatically closes the connection when done
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            System.out.println("Database initialized and 'messages' table is ready.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    // save a new message into the database
    public static void saveMessage(String senderAddress, String content) {
        String insertSQL = "INSERT INTO messages (sender_address, message_content) VALUES (?, ?)";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            // using PreparedStatement to prevent SQL Injection attacks
            pstmt.setString(1, senderAddress);
            pstmt.setString(2, content);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to save message to DB: " + e.getMessage());
        }
    }
}
