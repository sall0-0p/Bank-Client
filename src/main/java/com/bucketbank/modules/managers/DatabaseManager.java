package com.bucketbank.modules.managers;

import java.sql.SQLException;
import java.util.logging.Logger;

import com.bucketbank.Plugin;
import com.bucketbank.modules.database.NotificationsDatabase;
import org.bukkit.Bukkit;

public class DatabaseManager {
    private static final Plugin plugin = Plugin.getPlugin();
    private static final Logger logger = plugin.getLogger();

    // Databases
    private NotificationsDatabase notificationsDatabase;

    // Initialisation (constructor)
    public DatabaseManager() {
        try {

            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            notificationsDatabase = new NotificationsDatabase(plugin.getDataFolder().getAbsolutePath() + "/notifications.db");
    
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Failed to load Database");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    // Getters

    public NotificationsDatabase getNotificationsDatabase() {
        return notificationsDatabase;
    }

    // Close connections
    public void closeConnections() {
        try {
            notificationsDatabase.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
    }
}
