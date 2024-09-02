package com.bucketbank.commands.bucketfinance.account;

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
import com.bucketbank.modules.main.Account;
import com.bucketbank.modules.main.User;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class RemoveAccess implements Command {
    private static final App plugin = App.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();

    private Map<String, String> placeholders = new HashMap<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            if (!(sender instanceof Player)) {
                throw new Exception("Sender must be player!");
            }

            if (!sender.hasPermission("bucketfinance.account.user")) {
                throw new Exception("You have no permission to use this command!");
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
            UUID userId = player.getUniqueId();
            User requestedUser = new User(userId.toString());
            User senderUser = new User(((Player) sender).getUniqueId().toString());

            if (requestedUser.isDeleted() || requestedUser.isSuspended()) {
                throw new Exception("User is either deleted or suspended!");
            }

            Account account = new Account(args[0]);

            if (!account.hasAccess(senderUser) && !sender.hasPermission("bucketfinance.account.user.others")) {
                throw new Exception("Sender has no access to account!");
            }

            if (requestedUser.equals(senderUser)) {
                throw new Exception("You cannot remove yourself!");
            }

            account.removeUser(requestedUser);

            // Setup placeholders
            placeholders.put("%accountId%", account.getAccountId());
            placeholders.put("%username%", requestedUser.getUsername());

            // Print message
            String initialMessage = Messages.getString("account.removed_user");
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

