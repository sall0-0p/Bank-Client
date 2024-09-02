package com.bucketbank.modules.managers;

import java.sql.SQLException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

import com.bucketbank.App;
import com.bucketbank.database.AccountsDatabase;
import com.bucketbank.database.NotificationsDatabase;
import com.bucketbank.database.TransactionsDatabase;
import com.bucketbank.database.UsersDatabase;

public class DatabaseManager {
    private static final App plugin = App.getPlugin();
    private static final Logger logger = plugin.getLogger();

    // Databases
    private AccountsDatabase accountsDatabase;
    private UsersDatabase usersDatabase;
    private TransactionsDatabase transactionsDatabase;
    private NotificationsDatabase notificationsDatabase;

    // Initialisation (constructor)
    public DatabaseManager() {
        try {

            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
    
            accountsDatabase = new AccountsDatabase(plugin.getDataFolder().getAbsolutePath() + "/database.db");
            usersDatabase = new UsersDatabase(plugin.getDataFolder().getAbsolutePath() + "/database.db");
            transactionsDatabase = new TransactionsDatabase(plugin.getDataFolder().getAbsolutePath() + "/database.db");
            notificationsDatabase = new NotificationsDatabase(plugin.getDataFolder().getAbsolutePath() + "/database.db");
    
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Failed to load Database");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    // Getters
    public AccountsDatabase getAccountsDatabase() {
        return accountsDatabase;
    }

    public UsersDatabase getUsersDatabase() {
        return usersDatabase;
    }

    public TransactionsDatabase getTransactionsDatabase() {
        return transactionsDatabase;
    }

    public NotificationsDatabase getNotificationsDatabase() {
        return notificationsDatabase;
    }

    // Close connections
    public void closeConnections() {
        try {
            accountsDatabase.closeConnection();
            usersDatabase.closeConnection();
            transactionsDatabase.closeConnection();
            notificationsDatabase.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
    }
}
