package com.bucketbank.modules.main;

import java.lang.Exception;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.bucketbank.App;
import com.bucketbank.database.NotificationsDatabase;
import com.bucketbank.modules.managers.DatabaseManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Notification {
    private static App plugin = App.getPlugin();
    private static Logger logger = plugin.getLogger();
    private static DatabaseManager databaseManager = plugin.getDatabaseManager();
    private static NotificationsDatabase notificationsDatabase = databaseManager.getNotificationsDatabase();
    private static final MiniMessage mm = MiniMessage.miniMessage();

    private final String userId;
    private final String content;
    private final long timestamp;

    public Notification(String id, String content, long timestamp) {
        this.userId = id;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Notification(String id, String content, boolean createNew) throws Exception {
        if (createNew) {
            this.userId = id;
            this.content = content;

            try {
                this.timestamp = notificationsDatabase.createNotification(userId, content);
            } catch (Exception e) {
                throw e;
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(id));
            if (!(player.getPlayer() == null)) {
                try {
                    Component parsed = mm.deserialize(content);
                    player.getPlayer().sendMessage(parsed);
                    notificationsDatabase.markAsRead(id, content, timestamp);
                } catch (Exception e) {
                    throw e;
                }
            }
        } else {
            throw new Exception("Unable to create notification");
        }
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getContent() {
        return this.content;
    }

    public String getUserId() {
        return this.userId;
    }
}
