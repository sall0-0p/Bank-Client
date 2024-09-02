package com.bucketbank.commands.bucketfinance.atm;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bucketbank.App;
import com.bucketbank.database.UsersDatabase;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;
import com.bucketbank.modules.main.Account;
import com.bucketbank.modules.main.User;
import com.bucketbank.modules.managers.ATMManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class AtmCommand implements Command {
    private static final App plugin = App.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final UsersDatabase usersDatabase = (App.getDatabaseManager()).getUsersDatabase();
    private static ATMManager atmManager = App.getATMManager();
    String initialMessage;

    private Map<String, String> placeholders = new HashMap<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        String senderUUID = ((Player) sender).getUniqueId().toString();
        try {
            if (!(sender instanceof Player)) {
                throw new Exception("Sender must be player!");
            }
    
            if (!sender.hasPermission("bucketfinance.atm")) {
                throw new Exception("You have no permission to use this command!");
            }

            if (!atmManager.isPlayerNearATM((Player) sender) && !sender.hasPermission("bucketfinance.atm.remote")) {
                throw new Exception("You need to be near ATM to use this command!");
            }

            if (args.length > 0) {
                if (usersDatabase.userExists(senderUUID) && sender.hasPermission("bucketfinance.atm.advanced")) {
                    Account account = new Account(args[0]);
                    User user = new User(senderUUID);

                    if (account.hasAccess(user)) {
                        initialMessage = Messages.getString("atm.main_page");
                        placeholders.put("%accountId%", args[0]);
                    } else {
                        throw new Exception("You do not have access to this account!");
                    }
                }
            } else {
                if (usersDatabase.userExists(senderUUID) && sender.hasPermission("bucketfinance.atm.advanced")) {
                    initialMessage = Messages.getString("atm.main_page");
                    placeholders.put("%accountId%", new User(senderUUID).getPersonalAccountId());
                } else {
                    initialMessage = Messages.getString("atm.exchange_page");
                }
            }

            String parsedMessage = parsePlaceholders(initialMessage, placeholders);

            Component component = mm.deserialize(parsedMessage);
            sender.sendMessage(component);
        } catch (Exception e) {
            sender.sendMessage(mm.deserialize("<red>| " + e.getMessage()));
        }
    }

    private static String parsePlaceholders(String input, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            input = input.replace(entry.getKey(), entry.getValue());
        }
        return input;
    }
}
