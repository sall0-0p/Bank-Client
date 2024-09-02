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
import net.kyori.adventure.text.minimessage.MiniMessage;

public class WithdrawCommand implements Command {
    private static final MiniMessage mm = MiniMessage.miniMessage();

    App plugin = App.getPlugin();
    CurrencyManager currencyManager = plugin.getCurrencyManager();
    ATMManager atmManager = App.getATMManager();
    DiscordLogger discordLogger = App.getDiscordLogger();

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
                float amount = Integer.valueOf(args[1]);

                if (!account.isSuspended()) {
                    if (account.hasAccess(user) || sender.hasPermission("bucketfinance.atm.others")) {
                        if (account.getBalance() >= amount) {
                            boolean result = currencyManager.withdrawCurrency(player, (int) amount);
                        
                            if (result) {
                                account.modifyBalance(-amount);
                                player.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());

                                discordLogger.logRaw("atm", "{\"content\":null,\"embeds\":[{\"title\":\"Снятие со счета\",\"color\":13083000,\"fields\":[{\"name\":\"Игрок\",\"value\":\"" + player.getName() +"\"},{\"name\":\"Сума\",\"value\":\"" + String.valueOf(amount) + "\"}]}],\"attachments\":[]}");
                            }
                        } else {
                            throw new Exception("<red>| This account lacks funds!");
                        }
                    } else {
                        throw new Exception("<red>| You do not have access to this account!");
                    }
                } else {
                    throw new Exception("<red>| You are trying to access either suspended or deleted account!");
                }
            }
        } catch (Exception e) {
            sender.sendMessage(mm.deserialize(e.getMessage()));
        }

    }
}
