package com.bucketbank.commands.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.bucketbank.modules.account.Account;
import com.bucketbank.modules.user.User;
import com.bucketbank.modules.user.UserService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bucketbank.Plugin;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class AccountsCommand implements Command {
    private static final Plugin plugin = Plugin.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private final UserService userService = Plugin.getUserService();

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            if (!sender.hasPermission("bucketfinance.user.accounts")) {
                throw new Exception("You have no permission to use this command!");
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            UUID userId = player.getUniqueId();
            String username = player.getName();

            if (!((Player) sender).getUniqueId().toString().equals(player.getUniqueId().toString()) && !sender.hasPermission("bucketfinance.user.accounts.others")) {
                throw new Exception("You have no permission to view this user accounts!");
            }

            User user = userService.getUserByMinecraftUUIDAsync(player.getUniqueId()).get();
            List<Account> accounts = userService.getUserAccountsAsync(user.getId()).get();
            Map<String, String> placeholders = new HashMap<>();

            // define pages
            int pageCount = (int) Math.ceil((float) accounts.size() / 3) + 1;
            int currentPage;
            
            if (args.length == 1) {
                currentPage = 1;
            } else {
                currentPage = Integer.parseInt(args[1]);
            }

            List<Account> cutAccounts = getAccountsFromPage(accounts, currentPage);

            // Setup placeholders
            placeholders.put("%user%", username);
            placeholders.put("%userId%", userId.toString());
            placeholders.put("%account_limit%", String.valueOf(user.getAccountLimit()));
            placeholders.put("%account_count%", String.valueOf(accounts.size()));
            placeholders.put("%current_page%", String.valueOf(currentPage));
            placeholders.put("%page_count%", String.valueOf(pageCount));

            // Print message
            String header = Messages.getString("lists.accounts.header");
            String footer = Messages.getString("lists.accounts.footer");
            String body = "";

            for (Account account : cutAccounts) {
                body += parseListItem(account);
            }

            String initialMessage = header + body + footer;
            String parsedMessage = parsePlaceholders(initialMessage, placeholders);
            Component component = mm.deserialize(parsedMessage);
            sender.sendMessage(component);
        } catch (Exception e) {
            Component component = mm.deserialize(Messages.getString("command_failed") + "<newline>| " + e.getMessage());
            sender.sendMessage(component);
            e.printStackTrace();
        }
    }

    private static String parseListItem(Account account) {
        Map<String, String> placeholders = new HashMap<>();
        String initialBody = Messages.getString("lists.accounts.item");

        placeholders.put("%accountId%", account.getId());
        placeholders.put("%display_name%", account.getDisplayName()); 
        placeholders.put("%balance%", String.valueOf(account.getBalance()));
        if (account.isDeleted()) {
            placeholders.put("%status%", Messages.getString("account.status.deleted"));
        } else if (account.isSuspended()) {
            placeholders.put("%status%", Messages.getString("account.status.suspended"));
        } else {
            placeholders.put("%status%", Messages.getString("account.status.not_suspended"));
        }
        
        return parsePlaceholders(initialBody, placeholders);
    }
    
    private List<Account> getAccountsFromPage(List<Account> accounts, int page) {
        int itemsPerPage = 3;
        int fromIndex = (page - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, accounts.size());

        if (fromIndex >= accounts.size() || fromIndex < 0) {
            return new ArrayList<>(); // Return an empty list if the page number is out of bounds
        }

        return accounts.subList(fromIndex, toIndex);
    }

    private static String parsePlaceholders(String input, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            input = input.replace(entry.getKey(), entry.getValue());
        }
        return input;
    }
}
