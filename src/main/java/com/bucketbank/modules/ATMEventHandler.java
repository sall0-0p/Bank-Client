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

import com.bucketbank.App;

public class ATMEventHandler implements Listener {

    private final App plugin = App.getPlugin();
    private Set<String> atmLocations;
    private HashMap<UUID, Long> lastInteractionTime = new HashMap<>();
    private long debounceTime = 2000;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        this.atmLocations = new HashSet<>(plugin.getConfig().getStringList("data.atms"));

        long currentTime = System.currentTimeMillis();
        if (lastInteractionTime.containsKey(playerUUID)) {
            long lastTime = lastInteractionTime.get(playerUUID);
            if (currentTime - lastTime < debounceTime) {
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