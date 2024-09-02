package com.bucketbank.commands.bucketfinance;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bucketbank.App;
import com.bucketbank.commands.bucketfinance.account.HistoryCommand;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;
import com.bucketbank.modules.main.Account;
import com.bucketbank.modules.main.User;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class HistoryAliasCommand implements Command {
    private static final App plugin = App.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();

    private Map<String, String> placeholders = new HashMap<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            if (!sender.hasPermission("bucketfinance.account.history")) {
                throw new Exception("You have no permission to use this command!");
            }

            if (sender instanceof Player) {
                Player senderPlayer = (Player) sender;
                if (args.length == 0) {
                    User user = new User(senderPlayer.getUniqueId().toString());
                    String personalAccountId = user.getPersonalAccountId();

                    senderPlayer.performCommand("bf account " + personalAccountId + " history");
                } else {
                    switch (args.length) {
                        case 1:
                            if (Account.exists(args[0])) {
                                Account account = new Account(args[0]);

                                if (account.getOwnerId().equals(senderPlayer.getUniqueId().toString())) {
                                    senderPlayer.performCommand("bf account " + account.getAccountId() + " history");
                                } else {
                                    if (senderPlayer.hasPermission("bucketfinance.account.history.others")) {
                                        senderPlayer.performCommand("bf account" + args[0] + " history");
                                    } else {
                                        throw new Exception("You do not have permission to view history of this account.");
                                    }
                                }
                            } else {
                                throw new Exception("Such account does not exist!");
                            }
                        case 2:
                            if (Account.exists(args[0])) {
                                Account account = new Account(args[0]);

                                if (account.getOwnerId().equals(senderPlayer.getUniqueId().toString())) {
                                    senderPlayer.performCommand("bf account " + account.getAccountId() + " history " + args[1]);
                                } else {
                                    if (senderPlayer.hasPermission("bucketfinance.account.history.others")) {
                                        senderPlayer.performCommand("bf account" + args[0] + " history " + args[1]);
                                    } else {
                                        throw new Exception("You do not have permission to view history of this account.");
                                    }
                                }
                            } else {
                                throw new Exception("Such account does not exist!");
                            }
                    }
                }
            } else {
                throw new Exception("Command has to be issued by player!");
            }
        } catch (Exception e) {
            Component component = mm.deserialize(Messages.getString("command_failed") + "<newline>| " + e.getMessage());
            sender.sendMessage(component);
            e.printStackTrace();
        }
    }
}
