package com.bucketbank.commands.account.balance;

import java.util.HashMap;
import java.util.Map;

import com.bucketbank.modules.account.Account;
import com.bucketbank.modules.account.AccountService;
import com.bucketbank.modules.user.User;
import com.bucketbank.modules.user.UserService;
import org.bukkit.command.CommandSender;

import com.bucketbank.Plugin;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class SetBalanceCommand implements Command {
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final AccountService accountService = Plugin.getAccountService();

    private final Map<String, String> placeholders = new HashMap<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            if (args.length != 2) {
                throw new Exception("accountId and value have to be provided!");
            }

            if (!sender.hasPermission("bucketfinance.account.balance")) {
                throw new Exception("You have no permission to use this command!");
            }

            Account account;
            String messageType;
            if (isValidAccountId(args[0])) {
                if (accountService.accountExistsAsync(args[0]).get()) {
                    messageType = "balance_account";
                    account = accountService.getAccountAsync(args[0]).get();

                    try {
                        account.setBalance(Float.parseFloat(args[1]));
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new Exception("Invalid number or failed to set balance");
                    }
                } else {
                    throw new Exception("Account does not exist!");
                }
            } else {
                throw new Exception("This is not account id!");
            }

            User owner = account.getOwner();

            // Setup placeholders
            placeholders.put("%owner%", owner.getUsername());
            placeholders.put("%ownerId%", account.getId());
            placeholders.put("%accountId%", account.getId());
            placeholders.put("%balance%", String.valueOf(account.getBalance()));

            // Print message
            String initialMessage = Messages.getString("account.set_balance");
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
