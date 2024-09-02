package com.bucketbank.modules.managers;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.bucketbank.App;
import com.bucketbank.database.NotificationsDatabase;
import com.bucketbank.modules.main.Notification;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class NotificationManager implements Listener {
    private static final App plugin = App.getPlugin();
    private static final Logger logger = plugin.getLogger();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final DatabaseManager databaseManager = plugin.getDatabaseManager();
    private static final NotificationsDatabase notificationsDatabase = databaseManager.getNotificationsDatabase();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            Player sender = event.getPlayer();
            String playerId = sender.getUniqueId().toString();
            logger.info("Player joined with ID: " + playerId);

            List<Notification> notifications = notificationsDatabase.getUnreadNotifications(playerId);
            logger.info("Retrieved " + notifications.size() + " unread notifications for player ID: " + playerId);

            for (Notification notification : notifications) {
                try {
                    Component parsed = mm.deserialize(notification.getContent());
                    sender.sendMessage(parsed);
                    logger.info("Sent notification to player ID: " + playerId + " with content: " + notification.getContent());
                } catch (Exception e) {
                    logger.severe("Error parsing or sending notification content for player ID: " + playerId + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            logger.severe("Error handling player join event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
