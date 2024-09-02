package com.bucketbank.commands.bucketfinance.user;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bucketbank.App;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;
import com.bucketbank.modules.main.User;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class AboutUserCommand implements Command {
    private static final App plugin = App.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();

    private Map<String, String> placeholders = new HashMap<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            if (!sender.hasPermission("bucketfinance.user.about")) {
                throw new Exception("You have no permission to use this command!");
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            User user = new User(player);

            if (!((Player) sender).getUniqueId().toString().equals(player.getUniqueId().toString()) && !sender.hasPermission("bucketfinance.user.about.others")) {
                throw new Exception("You have no permission to view this user!");
            }

            // Setup placeholders
            placeholders.put("%user%", user.getUsername());
            placeholders.put("%userId%", user.getUserId());
            placeholders.put("%account%", user.getPersonalAccountId());

            // Parse time
            long userCreatedEpoch = user.getProfileCreatedTimestamp();
            Date userCreatedDate = new Date( userCreatedEpoch * 1000 );
            DateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");

            placeholders.put("%creation_date%", dateFormat.format(userCreatedDate));

            // Display account limit:
            String accountLimit = String.valueOf(user.getAccountLimit());
            if (accountLimit.equals("-1")) {
                placeholders.put("%account_limit%", "No Limit!");
            } else {
                placeholders.put("%account_limit%", accountLimit);
            }

            // Put buttons
            placeholders.put("%account_list%", Messages.getString("user.buttons.account_list"));
            
            // Status and Status related buttons
            if (user.isDeleted()) {
                placeholders.put("%status%", Messages.getString("user.status.deleted"));
                placeholders.put("%suspend%", "");
            } else if (user.isSuspended()) {
                placeholders.put("%status%", Messages.getString("user.status.suspended"));
                placeholders.put("%suspend%", Messages.getString("user.buttons.reinstate"));
            } else {
                placeholders.put("%status%", Messages.getString("user.status.not_suspended"));
                placeholders.put("%suspend%", Messages.getString("user.buttons.suspend"));
            }

            // Print message
            String initialMessage = Messages.getString("user.about");
            String parsedMessage = parsePlaceholders(initialMessage, placeholders);

            Component component = mm.deserialize(parsedMessage);
            sender.sendMessage(component);
        } catch (Exception e) {
            Component component = mm.deserialize(Messages.getString("command_failed") + "<newline>| " + e.getMessage());
            sender.sendMessage(component);
            e.printStackTrace();
        }
    }

    private static String parsePlaceholders(String input, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            input = input.replace(entry.getKey(), entry.getValue());
        }
        return input;
    }
}
