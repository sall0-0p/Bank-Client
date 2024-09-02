package com.bucketbank.modules.managers;

import java.util.List;

import com.bucketbank.App;
import com.bucketbank.database.AccountsDatabase;
import com.bucketbank.modules.main.Account;

public class CreditManager {
    private App plugin = App.getPlugin();
    private DatabaseManager databaseManager = App.getDatabaseManager();
    private AccountsDatabase accountsDatabase = databaseManager.getAccountsDatabase();

    long currentTime = System.currentTimeMillis() / 1000L;

    public CreditManager() {
        long interval = 72000L;
        plugin.getServer().getScheduler().runTaskTimer(plugin, new InterestUpdaterTask(), interval, interval);
        applyInterestForAllAccounts();
    }

    public class InterestUpdaterTask implements Runnable {
        @Override
        public void run() {
            applyInterestForAllAccounts();
        }
    }

    private void applyInterestForAllAccounts() {
        plugin.getLogger().info("Applying credit interest to accounts!");
        try {
            List<String> allAccountsIds = accountsDatabase.getAllAccounts();

            for (String accountId : allAccountsIds) {
                Account account = new Account(accountId);

                applyInterestForAccount(account);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyInterestForAccount(Account account) {
        long timeElapsed = currentTime - account.getTimeSinceLastInterestCalculation();
        float currentBalance = account.getBalance();

        if (currentBalance < 0) {
            float interest = calculateInterest(account, timeElapsed);

            try {
                plugin.getLogger().info("Applying interest of " + String.valueOf(interest) + " to account " + account.getAccountId());
                account.modifyBalance(-interest);

                accountsDatabase.updateLastInterestCalculation(account.getAccountId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private float calculateInterest(Account account, long timeElapsed) {
        float interestPercentage = account.getCreditPercent();
        float negativeBalance = Math.abs(account.getBalance());
        float interest = (float) Math.ceil(negativeBalance * (timeElapsed * ((interestPercentage / 100) / 604800)));

        return interest;
    }
}
