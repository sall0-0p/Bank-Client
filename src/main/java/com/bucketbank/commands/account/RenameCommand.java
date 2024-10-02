package com.bucketbank.commands.account;

import java.util.HashMap;
import java.util.Map;

import com.bucketbank.modules.account.Account;
import com.bucketbank.modules.account.AccountService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bucketbank.Plugin;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class RenameCommand implements Command {
    private static final Plugin plugin = Plugin.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final AccountService accountService = Plugin.getAccountService();

    private final Map<String, String> placeholders = new HashMap<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            if (!(sender instanceof Player)) {
                throw new Exception("Sender must be player!");
            }

            if (!sender.hasPermission("bucketfinance.account.rename")) {
                throw new Exception("You have no permission to use this command!");
            }

            Account account = accountService.getAccountAsync(args[0]).get();
            String name = concatenateArgs(args, 1);

            account.setDisplayName(name);

            // Setup placeholders
            placeholders.put("%accountId%", account.getId());

            // Print message
            String initialMessage = Messages.getString("account.renamed");
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

    private String concatenateArgs(String[] args, int number) {
        if (args.length >= number) {
            StringBuilder result = new StringBuilder();
            for (int i = number; i < args.length; i++) {
                if (i > number) {
                    result.append(" ");
                }
                result.append(args[i]);
            }
            return result.toString();
        } else {
            return Messages.getString("pay.default_note");
        }
    }
}
