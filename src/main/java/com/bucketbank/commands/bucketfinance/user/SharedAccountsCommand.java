package com.bucketbank.commands.bucketfinance.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bucketbank.App;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;
import com.bucketbank.modules.main.Account;
import com.bucketbank.modules.main.User;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class SharedAccountsCommand implements Command {
    private static final App plugin = App.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            if (!sender.hasPermission("bucketfinance.user.shared")) {
                throw new Exception("You have no permission to use this command!");
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            UUID userId = player.getUniqueId();
            String username = player.getName();

            if (!((Player) sender).getUniqueId().toString().equals(player.getUniqueId().toString()) && !sender.hasPermission("bucketfinance.user.accounts.others")) {
                throw new Exception("You have no permission to view this user accounts!");
            }

            User user = new User(player);
            List<String> accounts = getSharedAccountsOnly(user);
            Map<String, String> placeholders = new HashMap<>();

            // define pages
            int pageCount = (int) Math.ceil(accounts.size() / 3) + 1;
            int currentPage;
            
            if (args.length == 1) {
                currentPage = 1;
            } else {
                currentPage = Integer.parseInt(args[1]);
            }

            List<String> cutAccounts = getAccountsFromPage(accounts, currentPage);

            // Setup placeholders
            placeholders.put("%user%", username);
            placeholders.put("%userId%", userId.toString());
            placeholders.put("%account_limit%", String.valueOf(user.getAccountLimit()));
            placeholders.put("%account_count%", String.valueOf(accounts.size()));
            placeholders.put("%current_page%", String.valueOf(currentPage));
            placeholders.put("%page_count%", String.valueOf(pageCount));

            // Print message
            String header = Messages.getString("lists.accounts_shared.header");
            String footer = Messages.getString("lists.accounts_shared.footer");
            String body = "";

            for (String accountId : cutAccounts) {
                body += parseListItem(accountId);
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

    private static String parseListItem(String accountId) {
        Map<String, String> placeholders = new HashMap<>();
        String initialBody = Messages.getString("lists.accounts.item");
        
        Account account = new Account(accountId);

        placeholders.put("%accountId%", account.getAccountId());
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
    
    private List<String> getAccountsFromPage(List<String> accounts, int page) {
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

    public List<String> getSharedAccountsOnly(User user) {
        try {
            List<String> accessibleAccounts = new ArrayList<>(user.getAccessibleAccounts());
            List<String> ownedAccounts = user.getOwnedAccounts();

            accessibleAccounts.removeAll(ownedAccounts);

            return accessibleAccounts;
        } catch (Exception e) {
            e.printStackTrace();

            return new ArrayList<>();
        }
    }
}
