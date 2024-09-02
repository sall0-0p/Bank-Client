package com.bucketbank.modules.managers;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.bucketbank.App;

public class ATMManager {
    App plugin = App.getPlugin();
    FileConfiguration config = plugin.getConfig();

    public boolean isPlayerNearATM(Player player) {
        int range = config.getInt("atm_range");
        Location playerLocation = player.getLocation();

        for (String atmCoordinates : config.getStringList("data.atms")) {
            String[] parts = atmCoordinates.split(" ");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);

            Location atmLocation = new Location(player.getWorld(), x, y, z);

            if (playerLocation.distance(atmLocation) <= range) {
                return true;
            }
        }

        return false;
    }
}
