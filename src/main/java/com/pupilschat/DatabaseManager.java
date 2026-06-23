package com.pupilschat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5433/chatdb";
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

    //
    public static List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        String querySQL = "SELECT sender_address, message_content FROM messages ORDER BY created_at ASC";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(querySQL)) {

            while (rs.next()) {
                String sender = rs.getString("sender_address");
                String content = rs.getString("message_content");

                messages.add(new Message(sender, content));
            }

        } catch (SQLException e) {
            System.err.println("Failed to fetch messages from DB: " + e.getMessage());
        }

        return messages;
    }
}
