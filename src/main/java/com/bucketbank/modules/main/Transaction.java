package com.bucketbank.modules.main;

import java.sql.SQLException;

import org.bukkit.OfflinePlayer;

import com.bucketbank.App;
import com.bucketbank.database.TransactionsDatabase;
import com.bucketbank.modules.managers.DatabaseManager;

public class Transaction {
    private String transactionId;
    private String sourceAccountId;
    private String destinationAccountId;
    private float amount;
    private long timestamp;
    private String description;

    private DatabaseManager databaseManager = App.getPlugin().getDatabaseManager();
    private TransactionsDatabase transactionsDatabase = databaseManager.getTransactionsDatabase();

    // actual constructor

    private void buildTransaction(Account sourceAccount, Account destinationAccount, float amount, String description) throws SQLException {
        this.sourceAccountId = sourceAccount.getAccountId();
        this.destinationAccountId = destinationAccount.getAccountId();
        this.timestamp = (System.currentTimeMillis() / 1000L);
        this.amount = amount;
        this.description = description;
    }

    // create Transaction object from database

    public Transaction(String transactionId, String sourceAccountId, String destinationAccountId, float amount, long timestamp, String description) {
        this.transactionId = transactionId;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.timestamp = timestamp;
        this.description = description;
    }
    
    // adapters

    public Transaction(Account sourceAccount, Account destinationAccount, float amount, String description) throws SQLException {
        buildTransaction(sourceAccount, destinationAccount, amount, description);
    }

    public Transaction(String sourceAccountId, String destinationAccountId, float amount, String description) throws SQLException {
        Account sourceAccount = new Account(sourceAccountId);
        Account destinationAccount = new Account(destinationAccountId);

        buildTransaction(sourceAccount, destinationAccount, amount, description);
    }

    public Transaction(OfflinePlayer sourcePlayer, OfflinePlayer destinationPlayer, float amount, String description) {
        try {
            User sourceUser = new User(sourcePlayer);
            User destinationUser = new User(destinationPlayer);

            buildTransaction(sourceUser.getPersonalAccount(), destinationUser.getPersonalAccount(), amount, description);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // getters
    public String getTransactionId() {
        return transactionId;
    }

    public String getSourceAccountId() {
        return sourceAccountId;
    }

    public String getDestinationAccountId() {
        return destinationAccountId;
    }

    public float getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }
}
