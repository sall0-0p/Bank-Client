package com.bucketbank.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.bucketbank.App;
import com.bucketbank.modules.main.Notification;
import com.bucketbank.modules.managers.DatabaseManager;

public class NotificationsDatabase {
    private final Connection connection;
    private static final App plugin = App.getPlugin();
    private static final Logger logger = plugin.getLogger();

    public NotificationsDatabase(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        // Users table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS notifications (" +
                "notificationId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "userId TEXT NOT NULL, " +
                "content TEXT NOT NULL, " +
                "timestamp BIGINT NOT NULL, " +
                "read BOOLEAN DEFAULT 0" +
            ")");
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    };

    public long createNotification(String userId, String content) throws SQLException {
        String sql = "INSERT INTO notifications (userId, content, timestamp) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            long unixTimestamp = (System.currentTimeMillis() / 1000L);

            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, content);
            preparedStatement.setLong(3, unixTimestamp);
            
            preparedStatement.executeUpdate();

            return unixTimestamp;
        }
    }

    public List<Notification> getUnreadNotifications(String userId) throws SQLException {
        String selectSql = "SELECT userId, content, timestamp FROM notifications WHERE userId = ? AND read = 0";
        String updateSql = "UPDATE notifications SET read = 1 WHERE userId = ? AND content = ? AND timestamp = ?";
        List<Notification> unreadNotifications = new ArrayList<>();

        try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
            selectStatement.setString(1, userId);

            try (ResultSet resultSet = selectStatement.executeQuery()) {
                while (resultSet.next()) {
                    String id = resultSet.getString("userId");
                    String content = resultSet.getString("content");
                    long timestamp = resultSet.getLong("timestamp");

                    Notification notification = new Notification(id, content, timestamp);
                    unreadNotifications.add(notification);
                }
            }
        }

        // Mark notifications as read
        try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
            for (Notification notification : unreadNotifications) {
                updateStatement.setString(1, userId);
                updateStatement.setString(2, notification.getContent());
                updateStatement.setLong(3, notification.getTimestamp());
                updateStatement.addBatch();
            }
            updateStatement.executeBatch();
        }

        return unreadNotifications;
    }

    public void markAsRead(String userId, String content, long timestamp) {
        String updateSql = "UPDATE notifications SET read = 1 WHERE userId = ? AND content = ? AND timestamp = ?";

        try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
            updateStatement.setString(1, userId);
            updateStatement.setString(2, content);
            updateStatement.setLong(3, timestamp);
            updateStatement.addBatch();

            updateStatement.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
