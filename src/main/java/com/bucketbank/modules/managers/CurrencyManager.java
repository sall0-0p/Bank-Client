package com.bucketbank.modules.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.bucketbank.App;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class CurrencyManager {
    private static final MiniMessage mm = MiniMessage.miniMessage();
    App plugin = App.getPlugin();
    Logger logger = plugin.getLogger();

    public CurrencyManager() {}

    @SuppressWarnings("deprecation")
    public boolean validateCurrency(ItemStack item) {
        FileConfiguration config = plugin.getConfig();

        int CURRENCY_MODEL_DATA = config.getInt("currency.custom_model_data");
        String CURRENCY_NAME = config.getString("currency.name");
        String CURRENCY_MATERIAL = config.getString("currency.item");
    
        if (item != null && item.getType() == Material.getMaterial(CURRENCY_MATERIAL)) {
            // Check for specific name and lore
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                // TODO: Remove this
                if (ChatColor.stripColor(meta.getDisplayName()).equals(CURRENCY_NAME) || ChatColor.stripColor(meta.getDisplayName()).equals("Ⱥ Фрины")) {
                    if (meta.hasLore()) {
                        return true;
                    } else {
                        logger.info("No lore!");
                        return false;
                    }
                }
            }

            // Check for specific CustomModelData
            if (meta != null && meta.hasCustomModelData()) {
                if (meta.getCustomModelData() == CURRENCY_MODEL_DATA) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public boolean depositCurrency(Player player, int amount) {
        List<ItemStack> itemsToRemove = new ArrayList<>();
        int count = 0;
    
        if (player.getInventory().getContents() != null) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && validateCurrency(item)) {
                    itemsToRemove.add(item);
                    count += item.getAmount();
                    if (count >= amount) {
                        break;
                    }
                }
            }
        } else {
            logger.info("Player inventory is null.");
        }
    
        // Remove the items from the player's inventory
        if (count >= amount) {
            int remaining = amount;
            for (ItemStack item : itemsToRemove) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    player.getInventory().removeItem(item);
                    remaining -= itemAmount;
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
     
                if (remaining <= 0) {
                    break;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean withdrawCurrency(Player player, int amount) {
        FileConfiguration config = plugin.getConfig();

        String CURRENCY_MATERIAL = config.getString("currency.item");
        int CURRENCY_MODEL_DATA = config.getInt("currency.custom_model_data");
        String CURRENCY_NAME = config.getString("currency.name");
        List<String> CURRENCY_LORE = config.getStringList("currency.lore");
    
        List<ItemStack> currencyItems = new ArrayList<>();
    
        // Create ItemStacks of 64 until remaining amount is less than 64
        while (amount > 64) {
            ItemStack currencyItem = new ItemStack(Material.getMaterial(CURRENCY_MATERIAL), 64);
            ItemMeta meta = currencyItem.getItemMeta();
            
            if (meta != null) {
                meta.itemName(mm.deserialize(CURRENCY_NAME));
                meta.setCustomModelData(CURRENCY_MODEL_DATA);
    
                List<Component> lore = new ArrayList<>();
                for (String rawLoreLine : CURRENCY_LORE) {
                    lore.add(mm.deserialize(rawLoreLine));
                }
    
                if (!lore.isEmpty()) {
                    meta.lore(lore);
                }
    
                currencyItem.setItemMeta(meta);
            }
            
            currencyItems.add(currencyItem);
            amount -= 64;
        }
    
        // Add the remaining amount as the last ItemStack
        if (amount > 0) {
            ItemStack currencyItem = new ItemStack(Material.getMaterial(CURRENCY_MATERIAL), amount);
            ItemMeta meta = currencyItem.getItemMeta();
            
            if (meta != null) {
                meta.itemName(mm.deserialize(CURRENCY_NAME));
                meta.setCustomModelData(CURRENCY_MODEL_DATA);
    
                List<Component> lore = new ArrayList<>();
                for (String rawLoreLine : CURRENCY_LORE) {
                    lore.add(mm.deserialize(rawLoreLine));
                }
    
                if (!lore.isEmpty()) {
                    meta.lore(lore);
                }
    
                currencyItem.setItemMeta(meta);
            }
            
            currencyItems.add(currencyItem);
        }
    
        // Attempt to add the items to the player's inventory
        HashMap<Integer, ItemStack> remainingItems = new HashMap<>();
        for (ItemStack item : currencyItems) {
            remainingItems.putAll(player.getInventory().addItem(item));
        }
    
        if (remainingItems.isEmpty()) {
            return true;
        } else {
            // If there are remaining items, it means the inventory was full
            for (ItemStack item : remainingItems.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            return true;
        }
    }

    public int getDiamondsInInventory(Player player) {
        int count = 0;
    
        if (player.getInventory().getContents() != null) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && validateCurrency(item)) {
                    count += item.getAmount();
                }
            }
        } else {
            logger.info("Player inventory is null.");
        }
        
        return count;
    }

    public int getEmptySlots(Player player) {
        int count = 0;

        if (player.getInventory().getContents() != null) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item == null) {
                    count++;
                }
            }
        }
        
        return count;
    }
    
}    