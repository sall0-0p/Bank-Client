package com.bucketbank.commands.account.balance;

import java.util.HashMap;
import java.util.Map;

import com.bucketbank.modules.account.Account;
import com.bucketbank.modules.account.AccountService;
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

public class GetBalanceCommand implements Command {
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final AccountService accountService = Plugin.getAccountService();
    private static final UserService userService = Plugin.getUserService();

    private final Map<String, String> placeholders = new HashMap<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            if (args.length == 0) {
                throw new Exception("Account Id or Username has to be provided!");
            }

            if (!sender.hasPermission("bucketfinance.account.balance")) {
                throw new Exception("You have no permission to use this command!");
            }

            Account account;
            User owner;
            String messageType;
            if (isValidAccountId(args[0])) {
                if (accountService.accountExistsAsync(args[0]).get()) {
                    messageType = "balance_account";
                    account = accountService.getAccountAsync(args[0]).get();
                    owner = account.getOwner();
                } else {
                    throw new Exception("Account does not exist!");
                }
            } else {
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
                if (userService.userExistsWithMinecraftUUIDAsync(player.getUniqueId()).get()) {
                    owner = userService.getUserByMinecraftUUIDAsync(player.getUniqueId()).get();
                    messageType = "balance_user";
                    account = accountService.getAccountAsync(owner.getPersonalAccountId()).get();
                } else {
                    throw new Exception("User does not exist!");
                }
            }

            // Setup placeholders
            placeholders.put("%owner%", owner.getUsername());
            placeholders.put("%ownerId%", account.getId());
            placeholders.put("%accountId%", account.getId());
            placeholders.put("%balance%", String.valueOf(account.getBalance()));

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
}
