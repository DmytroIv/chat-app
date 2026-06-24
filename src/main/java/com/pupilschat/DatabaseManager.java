package com.pupilschat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = System.getenv().getOrDefault("SPRING_DATASOURCE_URL",
            "jdbc:postgresql://localhost:5433/chatdb");
    private static final String USER = System.getenv().getOrDefault("SPRING_DATASOURCE_USERNAME", "chatuser");
    private static final String PASSWORD = System.getenv().getOrDefault("SPRING_DATASOURCE_PASSWORD", "chatpassword");

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

            stmt.execute("ALTER TABLE messages ADD COLUMN IF NOT EXISTS room VARCHAR(50) DEFAULT 'general'");

            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(50) PRIMARY KEY, " +
                    "password VARCHAR(255) NOT NULL)");

            System.out.println("Database initialized and tables are ready.");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

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

    public static boolean createUser(String username, String password) {
        String insertSQL = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to create user: " + e.getMessage());
            return false;
        }
    }

    public static String getUserPassword(String username) {
        String querySQL = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(querySQL)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password");
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch user: " + e.getMessage());
        }
        return null; // Not found
    }
}
