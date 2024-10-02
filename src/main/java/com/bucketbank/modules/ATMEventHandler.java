package com.bucketbank.modules;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.bucketbank.Plugin;

public class ATMEventHandler implements Listener {

    private final Plugin plugin = Plugin.getPlugin();
    private final HashMap<UUID, Long> lastInteractionTime = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        long DEBOUNCE_TIME = 2000;

        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        Set<String> atmLocations = new HashSet<>(plugin.getConfig().getStringList("data.atms"));

        long currentTime = System.currentTimeMillis();
        if (lastInteractionTime.containsKey(playerUUID)) {
            long lastTime = lastInteractionTime.get(playerUUID);
            if (currentTime - lastTime < DEBOUNCE_TIME) {
                return;
            }
        }
        lastInteractionTime.put(playerUUID, currentTime);

        if (event.getClickedBlock() == null || event.getClickedBlock().getType() == Material.AIR) {
            return;
        }

        Block block = event.getClickedBlock();
        String locationString = block.getX() + " " + block.getY() + " " + block.getZ();

        if (atmLocations.contains(locationString)) {
            Bukkit.dispatchCommand(event.getPlayer(), "bf atm");
        }
    }
}