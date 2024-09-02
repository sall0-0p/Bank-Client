package com.bucketbank.commands.bucketfinance.account;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.bucketbank.App;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;
import com.bucketbank.modules.main.Account;
import com.bucketbank.modules.main.User;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class CreateAccountSpecialCommand implements Command {
    private static final App plugin = App.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();

    private Map<String, String> placeholders = new HashMap<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            UUID userId = player.getUniqueId();
            User requestedUser = new User(userId.toString());

            if (!sender.hasPermission("bucketfinance.account.create")) {
                throw new Exception("You have no permission to use this command!");
            }

            if (requestedUser.isDeleted() || requestedUser.isSuspended()) {
                throw new Exception("User is either deleted or suspended!");
            }

            Account account = new Account(userId.toString(), true, 0, 0, args[1]);

            // Setup placeholders
            placeholders.put("%owner%", new User(account.getOwnerId()).getUsername());
            placeholders.put("%ownerId%", account.getAccountId());
            placeholders.put("%accountId%", account.getAccountId());
            placeholders.put("%balance%", String.valueOf(account.getBalance()));

            if (args.length > 1) {
                String displayName = concatenateArgs(args, 2);
                account.setDisplayName(displayName);
            }

            placeholders.put("%display_name%", account.getDisplayName());

            // Print message
            String initialMessage = Messages.getString("account.created");
            String parsedMessage = parsePlaceholders(initialMessage, placeholders);

            Component component = mm.deserialize(parsedMessage);
            sender.sendMessage(component);
        } catch (Exception e) {
            Component component = mm.deserialize("<red>| " + e.getMessage());
            sender.sendMessage(component);
        }
    }

    private String concatenateArgs(String[] args, int number) {
        StringBuilder result = new StringBuilder();
        for (int i = number; i < args.length; i++) {
            if (i > number) {
                result.append(" ");
            }
            result.append(args[i]);
        }
        return result.toString();
    }

    private static String parsePlaceholders(String input, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            input = input.replace(entry.getKey(), entry.getValue());
        }
        return input;
    }
}

