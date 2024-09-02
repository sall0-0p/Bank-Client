package com.bucketbank.commands.bucketfinance.account;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bucketbank.App;
import com.bucketbank.database.TransactionsDatabase;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;
import com.bucketbank.modules.main.Account;
import com.bucketbank.modules.main.Transaction;
import com.bucketbank.modules.main.User;
import com.bucketbank.modules.managers.DatabaseManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class HistoryCommand implements Command {
    private static final App plugin = App.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final DatabaseManager databaseManager = plugin.getDatabaseManager();
    private static final TransactionsDatabase transactionsDatabase = databaseManager.getTransactionsDatabase();

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            if (!(sender instanceof Player)) {
                throw new Exception("Sender must be player!");
            }

            if (!sender.hasPermission("bucketfinance.account.history")) {
                throw new Exception("You have no permission to use this command!");
            }

            List<Transaction> transactions = transactionsDatabase.getTransactionsByAccountId(args[0]);
            Collections.reverse(transactions);
            Map<String, String> placeholders = new HashMap<>();

            // define pages
            int pageCount = (int) Math.ceil(transactions.size() / 5) + 1;
            int currentPage;
            
            if (args.length == 1) {
                currentPage = 1;
            } else {
                currentPage = Integer.parseInt(args[1]);
            }

            List<Transaction> cutTransactions = getTransactionsFromPage(transactions, currentPage);
            Account account = new Account(args[0]);
            User accountOwner = new User(account.getOwnerId());
            User senderUser = new User(((Player) sender).getUniqueId().toString());

            if (!account.hasAccess(senderUser) && !sender.hasPermission("bucketfinance.account.history.others")) {
                throw new Exception("Sender has no access to account!");
            }

            // Setup placeholders
            placeholders.put("%user%", accountOwner.getUsername());
            placeholders.put("%userId%", accountOwner.getUserId());
            placeholders.put("%transaction_count%", String.valueOf(transactions.size()));
            placeholders.put("%accountId%", account.getAccountId());
            placeholders.put("%displayName%", account.getDisplayName());
            placeholders.put("%current_page%", String.valueOf(currentPage));
            placeholders.put("%page_count%", String.valueOf(pageCount));

            // Print message
            String header = Messages.getString("lists.transactions.header");
            String footer = Messages.getString("lists.transactions.footer");
            String body = "";

            for (Transaction transaction : cutTransactions) {
                body += parseListItem(transaction, args[0]);
            }

            String initialMessage = header + body + footer;
            String parsedMessage = parsePlaceholders(initialMessage, placeholders);
            Component component = mm.deserialize(parsedMessage);
            sender.sendMessage(component);
        } catch (Exception e) {
            Component component = mm.deserialize("<red>| " + e.getMessage());
            sender.sendMessage(component);
        }
    }

    private static String parseListItem(Transaction transaction, String queryUserId) {
        Map<String, String> placeholders = new HashMap<>();
        String initialBody;
        
        long transactionEpoch = transaction.getTimestamp();
        Date transactionDate = new Date( transactionEpoch * 1000 );
        DateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");

        try {
            Account sourceAccount = new Account(transaction.getSourceAccountId());
            Account destinationAccount = new Account(transaction.getDestinationAccountId());
            User sourceUser = new User(sourceAccount.getOwnerId());
            User destinationUser = new User(destinationAccount.getOwnerId());

            placeholders.put("%source_player%", sourceUser.getUsername());
            placeholders.put("%destination_player%", destinationUser.getUsername());
            placeholders.put("%source_displayName%", sourceAccount.getDisplayName());
            placeholders.put("%destination_displayName%", destinationAccount.getDisplayName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        

        placeholders.put("%transactionId%", transaction.getTransactionId());
        placeholders.put("%sourceId%", transaction.getSourceAccountId());
        placeholders.put("%destinationId%", transaction.getDestinationAccountId());
        placeholders.put("%amount%", String.valueOf(transaction.getAmount()));
        placeholders.put("%description%", transaction.getDescription());
        placeholders.put("%timestamp%", dateFormat.format(transactionDate));

        if (queryUserId.equals(transaction.getSourceAccountId())) {
            initialBody = Messages.getString("lists.transactions.outbound_item");
        } else if (queryUserId.equals(transaction.getDestinationAccountId())) {
            initialBody = Messages.getString("lists.transactions.inbound_item");
        } else {
            initialBody = "";
        }

        return parsePlaceholders(initialBody, placeholders);
    }
    
    private List<Transaction> getTransactionsFromPage(List<Transaction> transactions, int page) {
        int itemsPerPage = 5;
        int fromIndex = (page - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, transactions.size());

        if (fromIndex >= transactions.size() || fromIndex < 0) {
            return new ArrayList<>(); // Return an empty list if the page number is out of bounds
        }

        return transactions.subList(fromIndex, toIndex);
    }

    private static String parsePlaceholders(String input, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            input = input.replace(entry.getKey(), entry.getValue());
        }
        return input;
    }
}
