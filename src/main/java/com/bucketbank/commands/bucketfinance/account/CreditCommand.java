package com.bucketbank.commands.bucketfinance.account;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bucketbank.App;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;
import com.bucketbank.modules.main.Account;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class CreditCommand implements Command {
    private static final App plugin = App.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();

    private Map<String, String> placeholders = new HashMap<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            Account account = new Account(args[0]);

            if (!(sender instanceof Player)) {
                throw new Exception("Sender must be player!");
            }

            if (!sender.hasPermission("bucketfinance.account.credit")) {
                throw new Exception("You have no permission to use this command!");
            }

            float creditLimit;
            float creditPercent;
            String messageType;
            if (args.length > 2) {
                creditLimit = Float.valueOf(args[1]);
                creditPercent = Float.valueOf(args[2]);
                account.setCreditLimit(creditLimit);
                account.setCreditPercent(creditPercent);

                messageType = "credit_changed";
            } else {
                creditLimit = account.getCreditLimit();
                creditPercent = account.getCreditPercent();

                messageType = "credit_viewed";
            }

            // Setup placeholders
            placeholders.put("%accountId%", account.getAccountId());
            placeholders.put("%credit_limit%", String.valueOf(creditLimit));
            placeholders.put("%credit_percent%", String.valueOf(creditPercent));

            // Print message
            String initialMessage = Messages.getString("account." + messageType);
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
