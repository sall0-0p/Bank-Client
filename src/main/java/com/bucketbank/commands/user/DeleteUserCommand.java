package com.bucketbank.commands.user;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.bucketbank.modules.user.User;
import com.bucketbank.modules.user.UserService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.bucketbank.Plugin;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class DeleteUserCommand implements Command {
    private static final Plugin plugin = Plugin.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final UserService userService = Plugin.getUserService();

    private Map<String, String> placeholders = new HashMap<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            if (!sender.hasPermission("bucketfinance.user.delete")) {
                throw new Exception("You have no permission to use this command!");
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            UUID userId = player.getUniqueId();
            String username = player.getName();

            User user = userService.getUserByMinecraftUUIDAsync(player.getUniqueId()).get();
            userService.deleteUser(user.getId());

            // Setup placeholders
            placeholders.put("%user%", username);
            placeholders.put("%userId%", userId.toString());

            // Print message
            String initialMessage = Messages.getString("user.deleted");
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
