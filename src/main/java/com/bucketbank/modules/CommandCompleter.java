package com.bucketbank.modules;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.bucketbank.App;
import com.bucketbank.database.AccountsDatabase;
import com.bucketbank.modules.main.User;
import com.bucketbank.modules.managers.DatabaseManager;

public class CommandCompleter implements TabCompleter {
    App plugin = App.getPlugin();
    Logger logger = plugin.getLogger();

    DatabaseManager databaseManager = App.getDatabaseManager();
    AccountsDatabase accountsDatabase = databaseManager.getAccountsDatabase();

    public CommandCompleter() {

    }

    @Override
    public List<String> onTabComplete( CommandSender sender, Command command, String label, String[] args) {
        // logger.info(String.valueOf(args.length));
        try {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length > 1) {
                    // bf balance [player]
                    if (args[0].equals("balance")) {
                        if (player.hasPermission("bucketfinance.balance.others")) {
                            return null;
                        } else {
                            return getListOfAccounts(player);
                        }
                    }
    
                    // bf pay [account] [account]
                    // bf pay [player]
                    if (args[0].equals("pay")) {
                        return null;
                    }
    
                    // bf about
                    if (args[0].equals("about")) {
                        return null;
                    }

                    if (args[0].equals("atm")) {
                        return getListOfAccounts(player);
                    }

                    if (args[0].equals("history")) {
                        return getListOfAccounts(player);
                    }
    
                    // bf user [user] subarguments...
                    if (args[0].equals("user")) {
                        // bf user ...
                        switch (args.length) {
                            case 2:
                                return null;
                            case 3:
                                // player entered nickname
                                // about, create, delete, suspend, reinstate, limit set, accounts
                                List<String> result = new ArrayList<>();
    
                                addIfHasPermission(player, result, "about", "bucketfinance.user.about");
                                addIfHasPermission(player, result, "create", "bucketfinance.user.create");
                                addIfHasPermission(player, result, "suspend", "bucketfinance.user.suspend");
                                addIfHasPermission(player, result, "reinstate", "bucketfinance.user.reinstate");
                                addIfHasPermission(player, result, "limit", "bucketfinance.user.limit");
                                addIfHasPermission(player, result, "accounts", "bucketfinance.user.accounts");
                                addIfHasPermission(player, result, "shared", "bucketfinance.user.shared");

                                return result;
                            case 4:
                                switch (args[2]) {
                                    case "about":
                                        return new ArrayList<>();
                                    case "create":
                                        return new ArrayList<>();
                                    case "suspend":
                                        return new ArrayList<>();
                                    case "reinstate":
                                        return new ArrayList<>();
                                    case "accounts":
                                        return new ArrayList<>();
                                    case "shared":
                                        return new ArrayList<>();
                                    case "limit":
                                        List<String> resultt = new ArrayList<>();
                                        resultt.add("set");
                                        return resultt;
                                }
                            case 5:
                                if (args[3].equals("set")) {
                                    return new ArrayList<>();
                                }
                            return null;
                        }
                    }

                    if (args[0].equals("account")) {
                        // bf account ...
                        switch (args.length) {
                            case 2:
                                if (player.hasPermission("bucketfinance.account.others")) {
                                    try {
                                        List<String> result = accountsDatabase.getAllAccounts();
                                        return result;
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                        return new ArrayList<>();
                                    } 
                                } else {
                                    return getListOfAccounts(player);
                                }
                                
                            case 3:
                                // create, balance set, history, user add, user remove, suspend, reinstate, credit
                                List<String> result = new ArrayList<>();

                                addIfHasPermission(player, result, "create", "bucketfinance.account.create");
                                addIfHasPermission(player, result, "balance", "bucketfinance.account.balance");
                                addIfHasPermission(player, result, "history", "bucketfinance.account.history");
                                addIfHasPermission(player, result, "user", "bucketfinance.account.user");
                                addIfHasPermission(player, result, "suspend", "bucketfinance.account.suspend");
                                addIfHasPermission(player, result, "reinstate", "bucketfinance.account.reinstate");
                                addIfHasPermission(player, result, "credit", "bucketfinance.account.credit");
                                addIfHasPermission(player, result, "rename", "bucketfinance.account.rename");
                                addIfHasPermission(player, result, "special", "bucketfinance.account.create");


                                return result;
                            case 4:
                                switch (args[2]) {
                                    case "create":
                                        return new ArrayList<>();
                                    case "special":
                                        List<String> resultttt = new ArrayList<>();
                                        resultttt.add("create");
                                        return resultttt;
                                    case "history":
                                        return new ArrayList<>();
                                    case "suspend":
                                        return new ArrayList<>();
                                    case "reinstate":
                                        return new ArrayList<>();
                                    case "credit":
                                        return new ArrayList<>();
                                    case "user":
                                        List<String> resultt = new ArrayList<>();

                                        resultt.add("add");
                                        resultt.add("remove");
                                        resultt.add("list");

                                        return resultt;
                                    case "balance":
                                        List<String> resulttt = new ArrayList<>();

                                        resulttt.add("set");
                                        resulttt.add("get");
                                        
                                        return resulttt;
                                    
                                    case "rename":
                                        return new ArrayList<>();
                                }
                            case 5:
                                switch (args[3]) {
                                    case "set":
                                        return new ArrayList<>();
                                    case "get":
                                        return new ArrayList<>();
                                    case "create":
                                        return new ArrayList<>();
                                }
                        }
                    }
                    
                } else {
                    List<String> result = new ArrayList<>();
                    addIfHasPermission(player, result, "balance", "bucketfinance.balance");
                    addIfHasPermission(player, result, "pay", "bucketfinance.pay");
                    addIfHasPermission(player, result, "about", "bucketfinance.about");
                    addIfHasPermission(player, result, "help", "bucketfinance.help");
                    addIfHasPermission(player, result, "history", "bucketfinance.account.history");
                    addIfHasPermission(player, result, "accounts", "bucketfinance.user.accounts");
        
                    // add permission
                    addIfHasPermission(player, result, "user", "bucketfinance.user");
                    addIfHasPermission(player, result, "account", "bucketfinance.account");
                    addIfHasPermission(player, result, "reload", "bucketfinance.reload");
                    addIfHasPermission(player, result, "atm", "bucketfinance.atm");
    
                    return result;
                }
            }
    
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addIfHasPermission(Player player, List<String> list, String command, String permission) {
        if (player.hasPermission(permission)) {
            list.add(command);
        }
    }

    private List<String> getListOfAccounts(Player player) {
        try {
            User user = new User(player.getUniqueId().toString());
            return user.getAccessibleAccounts();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        
    }
}
