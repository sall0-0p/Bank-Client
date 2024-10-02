package com.bucketbank.commands;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.bucketbank.modules.account.Account;
import com.bucketbank.modules.account.AccountService;
import com.bucketbank.modules.user.User;
import com.bucketbank.modules.user.UserService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.bucketbank.Plugin;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class BalanceCommand implements Command {
    private static final Plugin plugin = Plugin.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private final FileConfiguration config = plugin.getConfig();
    private final AccountService accountService = Plugin.getAccountService();
    private final UserService userService = Plugin.getUserService();

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
                    UUID senderUUID = ((Player) sender).getUniqueId();
                    User user = userService.getUserByMinecraftUUIDAsync(senderUUID).get();

                    messageType = "balance_self";
                    account = accountService.getAccountAsync(user.getPersonalAccountId()).get();
                } else {
                    throw new Exception("This command has to be issued by player in this way!");
                }
            } else {
                if (isValidAccountId(args[0])) {
                    if (accountService.accountExistsAsync(args[0]).get()) {
                        messageType = "balance_account";
                        account = accountService.getAccountAsync(args[0]).get();
                        if (sender instanceof Player) {
                            UUID senderUUID = ((Player) sender).getUniqueId();
                            User user = userService.getUserByMinecraftUUIDAsync(senderUUID).get();

                            if (!sender.hasPermission("bucketfinance.balance.others")) {
                                throw new Exception("You do not have access to this account!");
                            }
                        }
                    } else {
                        throw new Exception("Account does not exist!");
                    }
                } else {
                    UUID targetUUID = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
                    if (userService.userExistsWithMinecraftUUIDAsync(targetUUID).get()) {
                        User user = userService.getUserByMinecraftUUIDAsync(targetUUID).get();
                        
                        messageType = "balance_user";
                        account = accountService.getAccountAsync(user.getPersonalAccountId()).get();

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
            placeholders.put("%owner%", account.getOwner().getUsername());
            placeholders.put("%ownerId%", account.getId());
            placeholders.put("%accountId%", account.getId());
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
        if (accountId == null || accountId.length() != 8) {
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
