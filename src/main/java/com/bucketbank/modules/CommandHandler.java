package com.bucketbank.modules;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.bucketbank.App;
import com.bucketbank.commands.bucketfinance.AboutCommand;
import com.bucketbank.commands.bucketfinance.AccountsAliasCommand;
import com.bucketbank.commands.bucketfinance.BalanceCommand;
import com.bucketbank.commands.bucketfinance.HelpCommand;
import com.bucketbank.commands.bucketfinance.HistoryAliasCommand;
import com.bucketbank.commands.bucketfinance.PayCommand;
import com.bucketbank.commands.bucketfinance.ReloadConfig;
import com.bucketbank.commands.bucketfinance.account.CreateAccountCommand;
import com.bucketbank.commands.bucketfinance.account.CreateAccountSpecialCommand;
import com.bucketbank.commands.bucketfinance.account.CreditCommand;
import com.bucketbank.commands.bucketfinance.account.GiveAccess;
import com.bucketbank.commands.bucketfinance.account.HistoryCommand;
import com.bucketbank.commands.bucketfinance.account.ListUsers;
import com.bucketbank.commands.bucketfinance.account.ReinstateAccountCommand;
import com.bucketbank.commands.bucketfinance.account.RemoveAccess;
import com.bucketbank.commands.bucketfinance.account.RenameCommand;
import com.bucketbank.commands.bucketfinance.account.SuspendAccountCommand;
import com.bucketbank.commands.bucketfinance.account.balance.GetBalanceCommand;
import com.bucketbank.commands.bucketfinance.account.balance.SetBalanceCommand;
import com.bucketbank.commands.bucketfinance.atm.AtmCommand;
import com.bucketbank.commands.bucketfinance.atm.DepositCommand;
import com.bucketbank.commands.bucketfinance.atm.ExchangeCurrencyCommand;
import com.bucketbank.commands.bucketfinance.atm.ExchangeDiamondsCommand;
import com.bucketbank.commands.bucketfinance.atm.PageCommand;
import com.bucketbank.commands.bucketfinance.atm.UpdateCurrencyCommand;
import com.bucketbank.commands.bucketfinance.atm.WithdrawCommand;
import com.bucketbank.commands.bucketfinance.user.AboutUserCommand;
import com.bucketbank.commands.bucketfinance.user.AccountsCommand;
import com.bucketbank.commands.bucketfinance.user.CreateUserCommand;
import com.bucketbank.commands.bucketfinance.user.DeleteUserCommand;
import com.bucketbank.commands.bucketfinance.user.LimitAccountsCommand;
import com.bucketbank.commands.bucketfinance.user.ReinstateUserCommand;
import com.bucketbank.commands.bucketfinance.user.SharedAccountsCommand;
import com.bucketbank.commands.bucketfinance.user.SuspendUserCommand;

public class CommandHandler implements CommandExecutor {
    private final Map<String, Command> commands = new HashMap<>();
    private final App plugin = App.getPlugin();

    public CommandHandler() {
        // main
        commands.put("about", new AboutCommand());
        commands.put("reload", new ReloadConfig());
        commands.put("balance", new BalanceCommand());
        commands.put("pay", new PayCommand());
        commands.put("history", new HistoryAliasCommand());
        commands.put("accounts", new AccountsAliasCommand());
        commands.put("help", new HelpCommand());

        // user
        commands.put("user about", new AboutUserCommand());
        commands.put("user create", new CreateUserCommand());
        commands.put("user delete", new DeleteUserCommand());
        commands.put("user suspend", new SuspendUserCommand());
        commands.put("user reinstate", new ReinstateUserCommand());
        commands.put("user limit set", new LimitAccountsCommand());
        commands.put("user accounts", new AccountsCommand());
        commands.put("user shared", new SharedAccountsCommand());

        // account
        commands.put("account create", new CreateAccountCommand());
        commands.put("account special create", new CreateAccountSpecialCommand());
        commands.put("account balance get", new GetBalanceCommand());
        commands.put("account balance set", new SetBalanceCommand());
        commands.put("account history", new HistoryCommand());
        commands.put("account user add", new GiveAccess());
        commands.put("account user remove", new RemoveAccess());
        commands.put("account user list", new ListUsers());
        commands.put("account suspend", new SuspendAccountCommand());
        commands.put("account reinstate", new ReinstateAccountCommand());
        commands.put("account credit", new CreditCommand());
        commands.put("account rename", new RenameCommand());

        // atm
        commands.put("atm", new AtmCommand());
        commands.put("atmm page", new PageCommand());
        commands.put("atmm deposit", new DepositCommand());
        commands.put("atmm withdraw", new WithdrawCommand());
        commands.put("atmm exchange diamonds", new ExchangeDiamondsCommand());
        commands.put("atmm exchange currency", new ExchangeCurrencyCommand());
        commands.put("atmm exchange update", new UpdateCurrencyCommand());
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
