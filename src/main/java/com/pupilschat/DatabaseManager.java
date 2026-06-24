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
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS messages (" +
                    "id SERIAL PRIMARY KEY, " +
                    "sender_address VARCHAR(255), " +
                    "message_content TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // NEW: Safely upgrade our existing table to support rooms!
            stmt.execute("ALTER TABLE messages ADD COLUMN IF NOT EXISTS room VARCHAR(50) DEFAULT 'general'");

            System.out.println("Database initialized and 'messages' table is ready.");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    // save a new message into the database
    public static void saveMessage(String room, String sender, String content) {
        String insertSQL = "INSERT INTO messages (room, sender_address, message_content) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, room);
            pstmt.setString(2, sender);
            pstmt.setString(3, content);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to save message to DB: " + e.getMessage());
        }
    }

    //
    public static List<Message> getMessagesByRoom(String room) {
        List<Message> messages = new ArrayList<>();
        String querySQL = "SELECT room, sender_address, message_content FROM messages WHERE room = ? ORDER BY created_at ASC";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(querySQL)) {

            pstmt.setString(1, room);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String dbRoom = rs.getString("room");
                    String sender = rs.getString("sender_address");
                    String content = rs.getString("message_content");
                    messages.add(new Message(dbRoom, sender, content));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch messages: " + e.getMessage());
        }
        return messages;
    }
}
