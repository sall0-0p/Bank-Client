package com.bucketbank.modules;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.bucketbank.Plugin;
import com.bucketbank.commands.AboutCommand;
import com.bucketbank.commands.BalanceCommand;
import com.bucketbank.commands.HelpCommand;
import com.bucketbank.commands.PayCommand;
import com.bucketbank.commands.ReloadConfig;
import com.bucketbank.commands.account.CreateAccountCommand;
import com.bucketbank.commands.account.HistoryCommand;
import com.bucketbank.commands.account.ReinstateAccountCommand;
import com.bucketbank.commands.account.RenameCommand;
import com.bucketbank.commands.account.SuspendAccountCommand;
import com.bucketbank.commands.account.balance.GetBalanceCommand;
import com.bucketbank.commands.account.balance.SetBalanceCommand;
import com.bucketbank.commands.user.AboutUserCommand;
import com.bucketbank.commands.user.AccountsCommand;
import com.bucketbank.commands.user.CreateUserCommand;
import com.bucketbank.commands.user.DeleteUserCommand;
import com.bucketbank.commands.user.LimitAccountsCommand;
import com.bucketbank.commands.user.ReinstateUserCommand;
import com.bucketbank.commands.user.SuspendUserCommand;

public class CommandHandler implements CommandExecutor {
    private final Map<String, Command> commands = new HashMap<>();
    private final Plugin plugin = Plugin.getPlugin();

    public CommandHandler() {
        // main
        commands.put("about", new AboutCommand());
        commands.put("reload", new ReloadConfig());
        commands.put("balance", new BalanceCommand());
        commands.put("pay", new PayCommand());
        commands.put("help", new HelpCommand());

        // user
        commands.put("user about", new AboutUserCommand());
        commands.put("user create", new CreateUserCommand());
        commands.put("user delete", new DeleteUserCommand());
        commands.put("user suspend", new SuspendUserCommand());
        commands.put("user reinstate", new ReinstateUserCommand());
        commands.put("user limit set", new LimitAccountsCommand());
        commands.put("user accounts", new AccountsCommand());

        // account
        commands.put("account create", new CreateAccountCommand());
        commands.put("account balance get", new GetBalanceCommand());
        commands.put("account balance set", new SetBalanceCommand());
        commands.put("account history", new HistoryCommand());
        commands.put("account suspend", new SuspendAccountCommand());
        commands.put("account reinstate", new ReinstateAccountCommand());
        commands.put("account rename", new RenameCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        try {
            if (args.length == 0) {
                sender.sendMessage("| Usage: /bucketfinance <command> [sub-command] [arguments]");
                return true;
            }
    
            StringBuilder subCommandPath = new StringBuilder(args[0]);
            Command command = commands.get(subCommandPath.toString());
            String coreArgument = "";
    
            if (args[0].equals("account") || args[0].equals("user")) {
                int index = 2;

                while (command == null && index < args.length) {
                    subCommandPath.append(" ").append(args[index]);
                    command = commands.get(subCommandPath.toString());
                    index++;
                }
    
                coreArgument = args[1];

                String[] subCommandArgs = new String[args.length - index + 1];
                subCommandArgs[0] = coreArgument;
                for (int i = index; i < args.length; i++) {
                    subCommandArgs[i - index + 1] = args[i];
                }

                if (command == null) {
                    sender.sendMessage("| Unknown command. Type \"/help\" for help.");
                    return true;
                }

                command.execute(sender, subCommandArgs);
            } else {
                int index = 1;

                while (command == null && index < args.length) {
                    subCommandPath.append(" ").append(args[index]);
                    command = commands.get(subCommandPath.toString());
                    index++;
                }
        
                if (command == null) {
                    sender.sendMessage("| Unknown command. Type \"/help\" for help.");
                    return true;
                }
        
                // Create a new array for the remaining arguments
                String[] subCommandArgs = new String[args.length - index];
                System.arraycopy(args, index, subCommandArgs, 0, args.length - index);
        
                command.execute(sender, subCommandArgs);
            }
    
            return true;
        } catch (Exception e) {
            sender.sendMessage("| Command Handler failed, check console!");
            e.printStackTrace();
            return true;
        }
    }
}
