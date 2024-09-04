package com.bucketbank.commands.atm;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.bucketbank.Plugin;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.main.Account;
import com.bucketbank.modules.main.User;
import com.bucketbank.modules.managers.ATMManager;
import com.bucketbank.modules.managers.CurrencyManager;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ExchangeDiamondsCommand implements Command {
    private static final MiniMessage mm = MiniMessage.miniMessage();

    private Plugin plugin = Plugin.getPlugin();
    private FileConfiguration config = plugin.getConfig();
    CurrencyManager currencyManager = Plugin.getCurrencyManager();
    ATMManager atmManager = Plugin.getATMManager();
    DiscordLogger discordLogger = Plugin.getDiscordLogger();

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
                if (inventory.contains(Material.DIAMOND, amount)) {
                    int diamondsInEconomy = plugin.diamondsInEconomy;
                    int exchangeCoefficient = config.getInt("data.exchange_coefficient");

                    float minExchangeCourse = diamondsInEconomy / (float) exchangeCoefficient;
                    float maxExchangeCourse = (diamondsInEconomy + amount) / (float) exchangeCoefficient;

                    double exchangeCourse = ((minExchangeCourse + maxExchangeCourse) / 2);

                    int currencyToGive = (int) Math.floor(amount / exchangeCourse);
                    
                    if (config.getBoolean("data.compensate_to_accounts")) {
                        double compensation = (amount / exchangeCourse) - currencyToGive;
                        float roundedCompensation = Math.round(compensation * 100.0f) / 100.0f;

                        if (User.existsWithUsername(player.getName())) {
                            User user = new User(player.getUniqueId().toString());
                            Account personalAccount = user.getPersonalAccount();

                            personalAccount.modifyBalance(roundedCompensation);
                        } else {
                            User user = new User(Bukkit.getOfflinePlayer(player.getUniqueId()), true, 0f, 0f);
                            Account personalAccount = user.getPersonalAccount();

                            personalAccount.modifyBalance(roundedCompensation);
                        }
                    }

                    int slotsRequired = (int) Math.floor(currencyToGive / 64);
                    if (currencyManager.getEmptySlots(player) < slotsRequired) {
                        throw new Exception("You do not have enough space in inventory.");
                    }

                    if (currencyToGive > 0) {
                        // Play the sound
                        player.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());

                        // Remove diamonds from the player's inventory
                        removeDiamonds(inventory, (int) Math.ceil(currencyToGive * exchangeCourse));

                        // Update the player's currency
                        currencyManager.withdrawCurrency(player, currencyToGive);

                        plugin.diamondsInEconomy += amount;
                        plugin.currencyInEconomy += currencyToGive;

                        discordLogger.log("atm_diamonds", "Player " + player.getName() + " exchanged `" + String.valueOf(amount) + "` diamonds for `" + String.valueOf(currencyToGive) + "$`. With course of `" + String.valueOf(exchangeCourse) + "`");
                    }
                } else {
                    throw new Exception("<red>| You do not have enough diamonds in your inventory.");
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
