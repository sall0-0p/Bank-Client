package com.bucketbank.commands.bucketfinance.account;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bucketbank.App;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;
import com.bucketbank.modules.main.Account;
import com.bucketbank.modules.main.User;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ListUsers implements Command {
    private static final App plugin = App.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            if (!(sender instanceof Player)) {
                throw new Exception("Sender must be player!");
            }

            if (!sender.hasPermission("bucketfinance.account.user")) {
                throw new Exception("You have no permission to use this command!");
            }

            User senderUser = new User(((Player) sender).getUniqueId().toString());

            Account account = new Account(args[0]);

            if (account.isSuspended() || account.isDeleted()) {
                throw new Exception("Trying to access suspended account!");
            }

            if (!account.hasAccess(senderUser) && !sender.hasPermission("bucketfinance.account.user.others")) {
                throw new Exception("Sender has no access to account!");
            }

            List<User> users = account.getUsers();

            // Print message
            
            String initialMessage = Messages.getString("account.list_users.header");

            for (User user : users) {
                String initialContent = Messages.getString("account.list_users.item");
                Map<String, String> placeholders = new HashMap<>();

                placeholders.put("%user%", user.getUsername());
                String parsedContent = parsePlaceholders(initialContent, placeholders);

                initialMessage += parsedContent;
            }

            Map<String, String> placeholders = new HashMap<>();

            String parsedMessage = parsePlaceholders(initialMessage, placeholders);

            Component component = mm.deserialize(parsedMessage);
            sender.sendMessage(component);
        } catch (Exception e) {
            Component component = mm.deserialize("<red>| " + e.getMessage());
            sender.sendMessage(component);
        }
    }

    private static String parsePlaceholders(String input, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            input = input.replace(entry.getKey(), entry.getValue());
        }
        return input;
    }
}

