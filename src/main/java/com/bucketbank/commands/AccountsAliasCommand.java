package com.bucketbank.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bucketbank.Plugin;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class AccountsAliasCommand implements Command {
    private static final Plugin plugin = Plugin.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();

    private Map<String, String> placeholders = new HashMap<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            if (!sender.hasPermission("bucketfinance.user.accounts")) {
                throw new Exception("You have no permission to use this command!");
            }

            if (sender instanceof Player) {
                Player senderPlayer = (Player) sender;
                if (args.length == 0) {
                    senderPlayer.performCommand("bf user " + senderPlayer.getName() + " accounts");
                } else {
                    if (senderPlayer.hasPermission("bucketfinance.user.accounts.others") || senderPlayer.getName().equals(args[0])) {
                        senderPlayer.performCommand("bf user " + args[0] + " accounts");
                    } else {
                        throw new Exception("You do not have access to their accounts");
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
