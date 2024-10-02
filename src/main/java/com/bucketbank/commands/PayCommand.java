package com.bucketbank.commands;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.bucketbank.modules.account.Account;
import com.bucketbank.modules.account.AccountService;
import com.bucketbank.modules.notification.Notification;
import com.bucketbank.modules.transaction.TransactionService;
import com.bucketbank.modules.user.User;
import com.bucketbank.modules.user.UserService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.bucketbank.Plugin;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class PayCommand implements Command {
    private static final Plugin plugin = Plugin.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private final FileConfiguration config = plugin.getConfig();

    private final Map<String, String> placeholders = new HashMap<>();
    private final DecimalFormat decimalFormat = new DecimalFormat(config.getString("decimal_format"));

    private final UserService userService = Plugin.getUserService();
    private final AccountService accountService = Plugin.getAccountService();
    private final TransactionService transactionService = Plugin.getTransactionService();

    // # %sender% - eitherid of account or username (depending on command) of person who sent
    // # %receiver% - either id of account or username (depending on command) of person who sent
    // # %amount% - amount of money sent
    // # %description% - description

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            if (!sender.hasPermission("bucketfinance.pay")) {
                throw new Exception("You have no permission to use this command!");
            }

            String messageType;
            String receiverPlayerId;
            UUID senderUUID = ((Player) sender).getUniqueId();

            if (args.length > 2 && isPositiveInteger(args[2])) {
                if (isValidAccountId(args[0]) && isValidAccountId(args[1])) {
                    // bf pay accountId accountId amount reason | messageType: account_account

                    if (args[0].equals(args[1])) {
                        throw new Exception("You cannot send from account to same account!");
                    }

                    // transactionManager.createTransaction(args[0], args[1], Float.parseFloat(args[2]), concatenateArgs(args, 3));
                    transactionService.createTransaction(args[0], args[1], Float.parseFloat(args[2]), concatenateArgs(args, 3));
                    messageType = "account_account";
                    receiverPlayerId = accountService.getAccountAsync(args[1]).get().getOwner().getMinecraftUUID().toString();

                    placeholders.put("%sender%", args[0]);
                    placeholders.put("%receiver%", args[1]);
                } else if (isValidAccountId(args[0]) && !isValidAccountId(args[1])) {
                    // bf pay accountId username amount reason | messageType: account_username
                    OfflinePlayer destinationPlayer = Bukkit.getOfflinePlayer(args[1]);
                    User destinationUser = userService.getUserByMinecraftUUIDAsync(destinationPlayer.getUniqueId()).get();

                    if (args[0].equals(destinationUser.getPersonalAccountId())) {
                        throw new Exception("You cannot send from account to same account!");
                    }

                    transactionService.createTransaction(args[0], destinationUser.getPersonalAccountId(), Float.parseFloat(args[2]), concatenateArgs(args, 3));
                    messageType = "account_username";
                    receiverPlayerId = destinationUser.getMinecraftUUID().toString();

                    placeholders.put("%sender%", args[0]);
                    placeholders.put("%receiver%", destinationUser.getUsername());
                } else {
                    throw new Exception("Not proper arguments given!");
                }

                placeholders.put("%amount%", decimalFormat.format(Float.parseFloat(args[2])));
                placeholders.put("%description%", concatenateArgs(args, 3));
            } else if (isPositiveInteger(args[1])) {
                if (isValidAccountId(args[0])) {
                    // bf pay accountId amount reason | messageType: account
                    // new User(((Player) sender).getUniqueId().toString());

                    User senderUser = userService.getUserByMinecraftUUIDAsync(senderUUID).get();

                    // if (!(accountIsOkay(senderUser.getPersonalAccountId()) && accountIsOkay(args[1]))) {
                    //     throw new Exception("There is something wrong with one of the accounts");
                    // }

                    if (senderUser.getPersonalAccountId().equals(args[0])) {
                        throw new Exception("You cannot send from account to same account!");
                    }

                    transactionService.createTransaction(senderUser.getPersonalAccountId(), args[0], Float.parseFloat(args[1]), concatenateArgs(args, 2));
                    messageType = "account";
                    receiverPlayerId = accountService.getAccountAsync(args[0]).get().getOwner().getMinecraftUUID().toString();

                    placeholders.put("%sender%", senderUser.getUsername());
                    placeholders.put("%receiver%", args[0]);
                } else {
                    // bf pay username amount reason | messageType: username
                    User senderUser = userService.getUserByMinecraftUUIDAsync(senderUUID).get();

                    OfflinePlayer destinationPlayer = Bukkit.getOfflinePlayer(args[0]);
                    User destinationUser = userService.getUserByMinecraftUUIDAsync(destinationPlayer.getUniqueId()).get();

                    if (senderUser.getPersonalAccountId().equals(destinationUser.getPersonalAccountId())) {
                        throw new Exception("You cannot send from account to same account!");
                    }

                    transactionService.createTransaction(senderUser.getPersonalAccountId(), destinationUser.getPersonalAccountId(), Float.parseFloat(args[1]), concatenateArgs(args, 2));
                    messageType = "username";
                    receiverPlayerId = destinationUser.getMinecraftUUID().toString();

                    placeholders.put("%sender%", senderUser.getUsername());
                    placeholders.put("%receiver%", destinationUser.getUsername());
                }

                placeholders.put("%amount%", decimalFormat.format(Float.parseFloat(args[1])));
                placeholders.put("%description%", concatenateArgs(args, 2));
            } else {
                throw new Exception("Amount has to be properly specified!");
            }

            // Setup placeholders

            // Print message
            String initialMessage = Messages.getString("pay." + messageType);
            String parsedMessage = parsePlaceholders(initialMessage, placeholders);

            Component component = mm.deserialize(parsedMessage);
            sender.sendMessage(component);

            // Send message to receiver

            initialMessage = Messages.getString("pay_received." + messageType);
            parsedMessage = parsePlaceholders(initialMessage, placeholders);
            
            new Notification(receiverPlayerId, parsedMessage, true);
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

    private boolean isValidAccountId(String accountId) {
        if (accountId == null || accountId.length() != 6) {
            return false;
        }
        for (char c : accountId.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPositiveInteger(String str) {
        if (str == null) {
            return false;
        } else {
            try {
                float number = Float.parseFloat(str);
                return number > 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
}
