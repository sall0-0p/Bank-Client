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
import com.bucketbank.modules.managers.ATMManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class PageCommand implements Command {
    private static final App plugin = App.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final UsersDatabase usersDatabase = (plugin.getDatabaseManager()).getUsersDatabase();
    ATMManager atmManager = App.getATMManager();
    String initialMessage;

    private Map<String, String> placeholders = new HashMap<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Player player = (Player) sender;
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
            
            initialMessage = Messages.getString("atm." + args[0]);
            placeholders.put("%accountId%", args[1]);
            placeholders.put("%balance%", String.valueOf(new Account(args[1]).getBalance()));

            // player.playSound(Sound.sound(Key.key("ui.button.click"), Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());
            String parsedMessage = parsePlaceholders(initialMessage, placeholders);

            Component component = mm.deserialize(parsedMessage);
            sender.sendMessage(component);
        } catch (Exception e) {

        }
    }

    private static String parsePlaceholders(String input, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            input = input.replace(entry.getKey(), entry.getValue());
        }
        return input;
    }
}
