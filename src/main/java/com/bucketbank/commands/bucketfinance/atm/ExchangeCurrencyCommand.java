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

public class ExchangeCurrencyCommand implements Command {
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
                if (currencyManager.getDiamondsInInventory(player) >= amount) {
                    int diamondsInEconomy = plugin.diamondsInEconomy;
                    int exchangeCoefficient = config.getInt("data.exchange_coefficient");

                    float minExchangeCourse = diamondsInEconomy / (float) exchangeCoefficient;
                    float maxExchangeCourse = (diamondsInEconomy - amount) / (float) exchangeCoefficient;

                    double exchangeCourse = ((minExchangeCourse + maxExchangeCourse) / 2);
                    int diamondsToGive = (int) Math.floor(amount * exchangeCourse);

                    if (config.getBoolean("data.compensate_to_accounts")) {
                        double diamondsToCompensate = Math.floor(amount * exchangeCourse) - diamondsToGive;
                        double compensation = diamondsToCompensate / exchangeCourse;
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

                    int slotsRequired = (int) Math.floor(diamondsToGive / 64);
                    if (currencyManager.getEmptySlots(player) < slotsRequired) {
                        throw new Exception("You do not have enough space in inventory.");
                    }

                    if (diamondsToGive > 0) {
                        // Play the sound
                        giveDiamonds(player, diamondsToGive);
                        currencyManager.depositCurrency(player, amount);

                        plugin.diamondsInEconomy -= diamondsToGive;
                        plugin.currencyInEconomy -= amount;

                        player.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());

                        discordLogger.log("atm_currency", "Player " + player.getName() + " exchanged `" + String.valueOf(amount) + "$` for `" + String.valueOf(diamondsToGive) + "` diamonds. With course of `" + String.valueOf(exchangeCourse) + "`");
                    }
                } else {
                    throw new Exception("You have not enought currency in your inventory!");
                }
            } else {
                throw new Exception("Amount has to be between 1 and 256");
            }
        } catch (Exception e) {
            sender.sendMessage(mm.deserialize("<red>| " + e.getMessage()));
        }
    }

    public static void giveDiamonds(Player player, int amount) {
        PlayerInventory inventory = player.getInventory();
        
        while (amount > 0) {
            int stackSize = Math.min(amount, Material.DIAMOND.getMaxStackSize());
            ItemStack diamondStack = new ItemStack(Material.DIAMOND, stackSize);
            inventory.addItem(diamondStack);
            amount -= stackSize;
        }
    }
}
