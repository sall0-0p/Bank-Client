package com.bucketbank.modules.managers;

import java.util.logging.Logger;

import com.bucketbank.App;
import com.bucketbank.database.TransactionsDatabase;
import com.bucketbank.modules.main.Account;
import com.bucketbank.modules.main.Transaction;

public class TransactionManager {
    private App plugin = App.getPlugin();
    private Logger logger = plugin.getLogger();
    private TransactionsDatabase transactionsDatabase;
    
    public TransactionManager() {
        this.transactionsDatabase = plugin.getDatabaseManager().getTransactionsDatabase();
    }

    public String createTransaction(String sourceAccountId, String destinationAccountId, float amount, String description) throws Exception {
        if (!Account.exists(sourceAccountId) || !Account.exists(destinationAccountId)) {
            throw new Exception("One or both accounts do not exist!");
        }

        Account sourceAccount = new Account(sourceAccountId);
        Account destinationAccount = new Account(destinationAccountId);

        if (sourceAccount.getBalance() + sourceAccount.getCreditLimit() - amount < 0) {
            throw new Exception("Sender has insufficient funds!");
        }

        if (sourceAccount.isSuspended() || destinationAccount.isSuspended()) {
            throw new Exception("Cannot use suspended accounts!");
        }

        if (sourceAccount.isDeleted() || destinationAccount.isDeleted()) {
            throw new Exception("Cannot use deleted accounts!");
        }

        if (amount < 0.001) {
            throw new Exception("You cannot send less then 0.01!");
        }

        Transaction transaction = new Transaction(sourceAccount, destinationAccount, amount, description);
        processTransaction(transaction);

        return transaction.getTransactionId();
    }

    private void processTransaction(Transaction transaction) throws Exception {
        boolean completed = false;

        Account sourceAccount = new Account(transaction.getSourceAccountId());
        Account destinationAccount = new Account(transaction.getDestinationAccountId());

        try {
            sourceAccount.modifyBalance(-transaction.getAmount());

            destinationAccount.modifyBalance(transaction.getAmount());

            transactionsDatabase.saveTransaction(transaction);
            completed = true;
        } catch (Exception e) {
            // revert
            e.printStackTrace();
            if (!completed) {
                sourceAccount.modifyBalance(transaction.getAmount());
                sourceAccount.modifyBalance(-transaction.getAmount());
            }
        }
    }
}
