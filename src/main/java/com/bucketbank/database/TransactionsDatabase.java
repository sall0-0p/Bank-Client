package com.bucketbank.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.bucketbank.modules.main.Transaction;

public class TransactionsDatabase {
    private final Connection connection;

    public TransactionsDatabase(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS transactions (" + 
            "transactionId TEXT PRIMARY KEY, " +
            "sourceAccountId TEXT NOT NULL, " +
            "destinationAccountId TEXT NOT NULL, " +
            "amount FLOAT NOT NULL, " +
            "timestamp BIGINT NOT NULL, " +
            "description TINYTEXT" +
            ")");
        }
    }

    public String saveTransaction(Transaction transaction) throws SQLException {
        String uniqueId = String.valueOf(transaction.getTimestamp()) + "-" + generateTransactionId() + "-" + transaction.getSourceAccountId() + "-" + transaction.getDestinationAccountId();
        String sql = "INSERT INTO transactions (transactionId, sourceAccountId, destinationAccountId, amount, timestamp, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, uniqueId);
            preparedStatement.setString(2, transaction.getSourceAccountId());
            preparedStatement.setString(3, transaction.getDestinationAccountId());
            preparedStatement.setFloat(4, transaction.getAmount());
            preparedStatement.setLong(5, transaction.getTimestamp());
            preparedStatement.setString(6, transaction.getDescription());
            preparedStatement.executeUpdate();
        }

        return uniqueId;
    }

    public Transaction getTransaction(String transactionId) throws SQLException {
        String sql = "SELECT transactionId, sourceAccountId, destinationAccountId, amount, timestamp, description FROM transactions WHERE transactionId = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, transactionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String sourceAccountId = resultSet.getString("sourceAccountId");
                    String destinationAccountId = resultSet.getString("destinationAccountId");
                    float amount = resultSet.getFloat("amount");
                    long timestamp = resultSet.getLong("timestamp");
                    String description = resultSet.getString("description");

                    return new Transaction(transactionId, sourceAccountId, destinationAccountId, amount, timestamp, description);
                } else {
                    return null;
                }
            }
        }
    }

    public List<Transaction> getTransactionsByAccountId(String accountId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT transactionId, sourceAccountId, destinationAccountId, amount, timestamp, description " +
                    "FROM transactions WHERE sourceAccountId = ? OR destinationAccountId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, accountId);
            statement.setString(2, accountId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String transactionId = resultSet.getString("transactionId");
                    String sourceAccountId = resultSet.getString("sourceAccountId");
                    String destinationAccountId = resultSet.getString("destinationAccountId");
                    float amount = resultSet.getFloat("amount");
                    long timestamp = resultSet.getLong("timestamp");
                    String description = resultSet.getString("description");
                    transactions.add(new Transaction(transactionId, sourceAccountId, destinationAccountId, amount, timestamp, description));
                }
            }
        }
        return transactions;
    }

    public List<Transaction> getAllTransactions(String accountId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT transactionId, sourceAccountId, destinationAccountId, amount, timestamp, description FROM transactions";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, accountId);
            statement.setString(2, accountId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String transactionId = resultSet.getString("transactionId");
                    String sourceAccountId = resultSet.getString("sourceAccountId");
                    String destinationAccountId = resultSet.getString("destinationAccountId");
                    float amount = resultSet.getFloat("amount");
                    long timestamp = resultSet.getLong("timestamp");
                    String description = resultSet.getString("description");
                    transactions.add(new Transaction(transactionId, sourceAccountId, destinationAccountId, amount, timestamp, description));
                }
            }
        }
        return transactions;
    }

    public float getTotalInFlow(String accountId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT SUM(amount) AS totalInflow FROM transactions WHERE destinationAccountId = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, accountId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getFloat("totalInflow");
                }
            }
        }
        return 0;
    }

    public float getTotalOutFlow(String accountId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT SUM(amount) AS totalInflow FROM transactions WHERE sourceAccountId = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, accountId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getFloat("totalInflow");
                }
            }
        }
        return 0;
    }

    public List<Transaction> getRecentTransactions(String accountId, float limit) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT transactionId, sourceAccountId, destinationAccountId, amount, timestamp, description " +
                       "FROM transactions WHERE sourceAccountId = ? OR destinationAccountId = ? " +
                       "ORDER BY timestamp DESC LIMIT ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, accountId);
            statement.setString(2, accountId);
            statement.setFloat(3, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String transactionId = resultSet.getString("transactionId");
                    String sourceAccountId = resultSet.getString("sourceAccountId");
                    String destinationAccountId = resultSet.getString("destinationAccountId");
                    float amount = resultSet.getFloat("amount");
                    long timestamp = resultSet.getLong("timestamp");
                    String description = resultSet.getString("description");
                    transactions.add(new Transaction(transactionId, sourceAccountId, destinationAccountId, amount, timestamp, description));
                }
            }
        }
        return transactions;
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    };

    private String generateTransactionId() {
        double rawId = Math.floor((Math.random() * (9999 - 1000) + 1000));
        String accountIdNumber = String.valueOf((float) rawId);

        return accountIdNumber;
    };
}
