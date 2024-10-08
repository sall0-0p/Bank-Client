package com.bucketbank;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.bucketbank.modules.account.AccountService;
import com.bucketbank.modules.managers.*;
import com.bucketbank.modules.transaction.TransactionService;
import com.bucketbank.modules.user.UserService;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.bucketbank.modules.ATMEventHandler;
import com.bucketbank.modules.CommandHandler;
import com.bucketbank.modules.Messages;

public class Plugin extends JavaPlugin {
    private static final int currentConfigVersion = 1;

    private static Logger logger;
    private static DatabaseManager databaseManager;
    private static Plugin plugin;
    private static CurrencyManager currencyManager;
    private static FileConfiguration config;
    private static UserService userService;
    private static AccountService accountService;
    private static TransactionService transactionService;
    public int diamondsInEconomy;
    public int currencyInEconomy;

    private File dataFile;
    private FileConfiguration dataConfig;

    @Override
    public void onEnable() {
        // Logger for logs
        logger = getLogger();
        plugin = this;

        // Config loading
        saveDefaultConfig();
        config = getConfig();
        if (config.getInt("config_version") != currentConfigVersion) {
            logger.severe("Running with wrong config version, please update your config!");
        }

        loadDataFile();
        loadEconomyData();

        new Messages();

        currencyManager = new CurrencyManager();

        // Init Databases
        databaseManager = new DatabaseManager();

        // Register commands
        this.getCommand("bucketfinance").setExecutor(new CommandHandler());
        getServer().getPluginManager().registerEvents(new NotificationManager(), this);
        getServer().getPluginManager().registerEvents(new ATMEventHandler(), this);

        userService = new UserService();
        accountService = new AccountService();
        transactionService = new TransactionService();

        // Final Log
        logger.info("Banking loaded!");
    }

    @Override
    public void onDisable() {
        databaseManager.closeConnections();
        saveEconomyData();

        // Final log
        logger.info("Banking unloaded");
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public static CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    public static UserService getUserService() {
        return userService;
    }

    public static Logger getStaticLogger() {
        return logger;
    }

    public static AccountService getAccountService() {
        return accountService;
    }

    public static TransactionService getTransactionService() {
        return transactionService;
    }

    // Data files

    private void loadDataFile() {
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            saveResource("data.yml", false);
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void loadEconomyData() {
        if (dataConfig.contains("currency_in_economy")) {
            currencyInEconomy = dataConfig.getInt("currency_in_economy");
        } else {
            currencyInEconomy = 0;
        }

        if (dataConfig.contains("diamonds_in_economy")) {
            diamondsInEconomy = dataConfig.getInt("diamonds_in_economy");
        } else {
            diamondsInEconomy = 0;
        }
    }

    private void saveEconomyData() {
        dataConfig.set("currency_in_economy", currencyInEconomy);
        dataConfig.set("diamonds_in_economy", diamondsInEconomy);

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            getLogger().severe("Could not save data to " + dataFile);
            e.printStackTrace();
        }
    }
}
