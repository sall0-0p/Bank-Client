package com.bucketbank.commands;

import java.util.logging.Logger;

import org.bukkit.command.CommandSender;

import com.bucketbank.Plugin;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ReloadConfig implements Command {
    private static final Plugin plugin = Plugin.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final Logger logger = plugin.getLogger();

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            if (!sender.hasPermission("bucketfinance.reload")) {
                throw new Exception("You have no permission to use this command!");
            }

            plugin.reloadConfig();
            Messages.reloadConfig();

            // message
            Component parsed = mm.deserialize(Messages.getString("reloaded"));
            sender.sendMessage(parsed);
        } catch (Exception e) {
            // error message
            Component parsed = mm.deserialize(Messages.getString("reload_failed"));
            sender.sendMessage(parsed);
            e.printStackTrace();
        }
    }
}
