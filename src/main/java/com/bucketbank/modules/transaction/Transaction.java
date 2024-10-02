package com.bucketbank.modules.transaction;

import java.util.Map;

public class Transaction {
    private final String transactionId;
    private final String sourceAccountId;
    private final String destinationAccountId;
    private final float amount;
    private final long timestamp;
    private final String description;

    // transaction
    public Transaction(Map<String, Object> data) {
        transactionId = data.get("transactionId").toString();
        sourceAccountId = data.get("sourceAccountId").toString();
        destinationAccountId = data.get("destinationAccountId").toString();
        amount = Float.parseFloat(data.get("amount").toString());
        timestamp = Long.parseLong(data.get("timestamp").toString());
        description = data.get("description").toString();
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
