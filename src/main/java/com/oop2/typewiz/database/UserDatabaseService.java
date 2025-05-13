package com.oop2.typewiz.database;

import java.sql.*;

import static com.oop2.typewiz.database.DatabaseConnection.getConnection;

public class UserDatabaseService {


    private static final String DB_URL = "jdbc:mysql://localhost:3306/typerush";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";
    private static UserDatabaseService instance;

    private UserDatabaseService() {
    }

    public static UserDatabaseService getInstance() {
        if (instance == null) {
            instance = new UserDatabaseService();
        }
        return instance;
    }

    public static boolean registerUser(String username, String email, String password) {
        String query = "INSERT INTO users (username, email, password, created_at) VALUES (?, ?, ?, NOW())";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


        public class User {
            private String username;
            private int highScore;

            public User(String username, int highScore) {
                this.username = username;
                this.highScore = highScore;
            }

            public String getUsername() {
                return username;
            }

            public int getHighScore() {
                return highScore;
            }
        }


    public User loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int highScore = rs.getInt("high_score");
                return new User(username, highScore);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }



}
