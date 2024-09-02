package com.bucketbank.modules;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bucketbank.Plugin;

public class Messages {
    private static final Plugin plugin = Plugin.getPlugin();

    private static File customConfigFile;
    private static FileConfiguration customConfig;

    public Messages() {
        customConfigFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            plugin.saveResource("messages.yml", false);
        }

        customConfig = new YamlConfiguration();

        try {
            customConfig.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    } 

    public static String getString(String key) {
        return customConfig.getString(key);
    }

    public static void reloadConfig() throws InvalidConfigurationException {
        try {
            customConfig.load(customConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
