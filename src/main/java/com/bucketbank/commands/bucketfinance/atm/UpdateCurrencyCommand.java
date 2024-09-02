package com.bucketbank.commands.bucketfinance.atm;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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

public class UpdateCurrencyCommand implements Command {
    private static final MiniMessage mm = MiniMessage.miniMessage();

    private App plugin = App.getPlugin();
    private FileConfiguration config = plugin.getConfig();
    CurrencyManager currencyManager = App.getCurrencyManager();
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
            PlayerInventory inventory = player.getInventory();
            int amount = Integer.parseInt(args[0]);

            if (amount > 0 && amount <= 256) {
                int currencyToGive = (int) Math.floor(amount);

                if (currencyToGive > 0) {
                    // Play the sound
                    player.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());

                    // Update the player's currency
                    if (currencyManager.depositCurrency(player, currencyToGive)) {
                        currencyManager.withdrawCurrency(player, currencyToGive);
                    }
                    
                    discordLogger.log("atm_update", "Player " + player.getName() + " updated `" + String.valueOf(amount) + "$`");
                }
            } else {
                throw new Exception("<red>| Amount has to be between 1 and 256!");
            }
        } catch (Exception e) {
            sender.sendMessage(mm.deserialize(e.getMessage()));
        }
    }

    private void removeDiamonds(PlayerInventory inventory, int amount) {
        int remaining = amount;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == Material.DIAMOND) {
                int count = item.getAmount();
                if (count <= remaining) {
                    inventory.removeItem(item);
                    remaining -= count;
                } else {
                    item.setAmount(count - remaining);
                    remaining = 0;
                }
                if (remaining <= 0) {
                    break;
                }
            }
        }
    }
}
