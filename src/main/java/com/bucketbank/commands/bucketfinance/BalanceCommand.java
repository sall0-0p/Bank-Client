package com.bucketbank.commands.bucketfinance;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.bucketbank.App;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;
import com.bucketbank.modules.main.Account;
import com.bucketbank.modules.main.User;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class BalanceCommand implements Command {
    private static final App plugin = App.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private final FileConfiguration config = plugin.getConfig();

    private Map<String, String> placeholders = new HashMap<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            if (!sender.hasPermission("bucketfinance.balance")) {
                throw new Exception("You have no permission to use this command!");
            }

            Account account;
            String messageType;

            if (args.length == (int) 0) {
                if (sender instanceof Player) {
                    User user = new User(((Player) sender).getUniqueId().toString());

                    messageType = "balance_self";
                    account = new Account(user.getPersonalAccountId());
                } else {
                    throw new Exception("This command has to be issued by player in this way!");
                }
            } else {
                if (isValidAccountId(args[0])) {
                    if (Account.exists(args[0])) {
                        messageType = "balance_account";
                        account = new Account(args[0]);
                        if (sender instanceof Player) {
                            User user = new User(((Player) sender).getUniqueId().toString());

                            if (!account.hasAccess(user) && !sender.hasPermission("bucketfinance.balance.others")) {
                                throw new Exception("You do not have access to this account!");
                            }
                        }
                    } else {
                        throw new Exception("Account does not exist!");
                    }
                } else {
                    if (User.existsWithUsername(args[0])) {
                        User user = new User(Bukkit.getOfflinePlayer(args[0]));
                        
                        messageType = "balance_user";
                        account = new Account(user.getPersonalAccountId());

                        if (sender instanceof Player) {
                            if (!((Player) sender).getName().equals(args[0]) && !sender.hasPermission("bucketfinance.balance.others")) {
                                throw new Exception("You have no permission to view this user accounts!");
                            }
                        }
                    } else {
                        throw new Exception("User does not exist!");
                    }
                }
            }

            DecimalFormat decimalFormat = new DecimalFormat(config.getString("decimal_format"));
            String balanceString = decimalFormat.format(account.getBalance());

            if (account.getBalance() < 0) {
                balanceString = "<red><bold>" + balanceString + "<reset>";
            }

            // Setup placeholders
            placeholders.put("%owner%", new User(account.getOwnerId()).getUsername());
            placeholders.put("%ownerId%", account.getAccountId());
            placeholders.put("%accountId%", account.getAccountId());
            placeholders.put("%balance%", balanceString);

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
