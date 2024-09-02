package com.bucketbank.commands.bucketfinance.atm;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bucketbank.App;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.DiscordLogger;
import com.bucketbank.modules.main.Account;
import com.bucketbank.modules.main.User;
import com.bucketbank.modules.managers.ATMManager;
import com.bucketbank.modules.managers.CurrencyManager;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class DepositCommand implements Command {
    App plugin = App.getPlugin();
    CurrencyManager currencyManager = plugin.getCurrencyManager();
    DiscordLogger discordLogger = App.getDiscordLogger();
    ATMManager atmManager = App.getATMManager();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    
    @Override
    public void execute(CommandSender sender, String[] args) {
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

            Player player = (Player) sender;

            if (args.length > 1) {
                User user = new User(player.getUniqueId().toString());
                Account account = new Account(args[0]);
                float amount = Integer.parseInt(args[1]);

                if (!account.isSuspended()) {
                    if (account.hasAccess(user) || sender.hasPermission("bucketfinance.atm.others")) {
                        boolean result = currencyManager.depositCurrency(player, (int) amount);
                    
                        if (result) {
                            account.modifyBalance(amount);
                            player.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());

                            discordLogger.logRaw("atm", "{\"content\":null,\"embeds\":[{\"title\":\"Пополнение счета\",\"color\":7914695,\"fields\":[{\"name\":\"Игрок\",\"value\":\"" + player.getName() +"\"},{\"name\":\"Сума\",\"value\":\"" + String.valueOf(amount) + "\"}]}],\"attachments\":[]}");
                        }
                    } else {
                        throw new Exception("<red>| You do not have access to this account!");
                    }
                } else {
                    throw new Exception("<red>| You are trying to access either suspended or deleted account!");
                }
            }

        } catch (Exception e) {
            Component component = mm.deserialize("<red>| " + e.getMessage());
            sender.sendMessage(component);
        }
    }
}
