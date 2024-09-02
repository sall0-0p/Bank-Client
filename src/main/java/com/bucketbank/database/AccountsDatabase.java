package com.bucketbank.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountsDatabase {
    private final Connection connection;

    public AccountsDatabase(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        // Accounts Table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                "accountId TEXT PRIMARY KEY, " +
                "displayName TEXT DEFAULT Account, " +
                "ownerId TEXT NOT NULL, " +
                "balance FLOAT NOT NULL DEFAULT 0, " +
                "suspended BOOL DEFAULT 0, " +
                "creditLimit FLOAT DEFAULT 0, " +
                "creditPercent FLOAT DEFAULT 0, " +
                "accountCreatedTimestamp BIGINT NOT NULL, " +
                "lastInterestCalculation BIGINT, " +
                "deleted BOOL DEFAULT 0" +
                ")");
        }

        // Access Table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS account_access (" +
                "accountId TEXT NOT NULL, " +
                "userId TEXT NOT NULL" +
                ")");
        }
    };

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    };

    // constructor
    public String createAccount(String accountOwnerId) throws SQLException {
        String accountId = generateAccountId();
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO accounts (accountId, ownerId, accountCreatedTimestamp) VALUES (?, ?, ?)")) {
            preparedStatement.setString(1, accountId);
            preparedStatement.setString(2, accountOwnerId);
            preparedStatement.setLong(3, System.currentTimeMillis() / 1000L);
            preparedStatement.executeUpdate();

            // adding user to people with access
            addAccessToAccount(accountId, accountOwnerId);

            return accountId;
        }
    };

    public String createAccount(String accountOwnerId, String customId) throws SQLException {
        String accountId = customId;

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO accounts (accountId, ownerId, accountCreatedTimestamp) VALUES (?, ?, ?)")) {
            preparedStatement.setString(1, accountId);
            preparedStatement.setString(2, accountOwnerId);
            preparedStatement.setLong(3, System.currentTimeMillis() / 1000L);
            preparedStatement.executeUpdate();

            // adding user to people with access
            addAccessToAccount(accountId, accountOwnerId);

            return accountId;
        }
    };

    // delete account
    public void deleteAccount(String accountId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE accounts SET deleted = ? WHERE accountId = ?")) {
            preparedStatement.setBoolean(1, true);
            preparedStatement.setString(2, accountId);
            preparedStatement.executeUpdate();
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM account_access WHERE accountId = ?")) {
            preparedStatement.setString(1, accountId);
            preparedStatement.executeUpdate();
        }
    };

    // check if exists
    public boolean accountExists(String accountId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM accounts WHERE accountId = ?")) {
            preparedStatement.setString(1, accountId);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            return resultSet.next();
        }
    };

    // setters
    public void setBalance(String accountId, float balance) throws SQLException {
        if (accountExists(accountId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE accounts SET balance = ? WHERE accountId = ?")) {
                preparedStatement.setFloat(1, balance);
                preparedStatement.setString(2, accountId);
                preparedStatement.executeUpdate();
            }
        }
    };

    public void setOwner(String accountId, String uuid) throws SQLException {
        if (accountExists(accountId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE accounts SET ownerId = ? WHERE accountId = ?")) {
                preparedStatement.setString(1, uuid);
                preparedStatement.setString(2, accountId);
                preparedStatement.executeUpdate();
            }
        }
    }

    public void setDisplayName(String accountId, String name) throws SQLException {
        if (accountExists(accountId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE accounts SET displayName = ? WHERE accountId = ?")) {
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, accountId);
                preparedStatement.executeUpdate();
            }
        }
    }

    public void setSuspendedStatus(String accountId, boolean freezed) throws SQLException {
        if (accountExists(accountId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE accounts SET suspended = ? WHERE accountId = ?")) {
                preparedStatement.setBoolean(1, freezed);
                preparedStatement.setString(2, accountId);
                preparedStatement.executeUpdate();
            }
        }
    }

    public void setCreditLimit(String accountId, float creditLimit) throws SQLException {
        String query = "UPDATE accounts SET creditLimit = ? WHERE accountId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setFloat(1, creditLimit);
            statement.setString(2, accountId);
            statement.executeUpdate();
        }
    }

    public void setCreditPercent(String accountId, float creditPercent) throws SQLException {
        String query = "UPDATE accounts SET creditPercent = ? WHERE accountId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setFloat(1, creditPercent);
            statement.setString(2, accountId);
            statement.executeUpdate();
        }
    }

    public void updateLastInterestCalculation(String accountId) throws SQLException {
        long currentTimestamp = System.currentTimeMillis() / 1000L; // Current epoch timestamp in seconds
        String query = "UPDATE accounts SET lastInterestCalculation = ? WHERE accountId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, currentTimestamp);
            statement.setString(2, accountId);
            statement.executeUpdate();
        }
    }

    // getters
    public float getBalance(String accountId) throws SQLException {
        if (accountExists(accountId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT balance FROM accounts WHERE accountId = ?")) {
                preparedStatement.setString(1, accountId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getFloat("balance");
                } else {
                    return 0;
                }
            }
        } else {
            return 0;
        }
    }

    public String getOwner(String accountId) throws SQLException {
        if (accountExists(accountId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT ownerId FROM accounts WHERE accountId = ?")) {
                preparedStatement.setString(1, accountId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getString("ownerId");
                } else {
                    return "";
                }
            }
        } else {
            return "";
        }
    }

    public String getDisplayName(String accountId) throws SQLException {
        if (accountExists(accountId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT displayName FROM accounts WHERE accountId = ?")) {
                preparedStatement.setString(1, accountId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getString("displayName");
                } else {
                    return "";
                }
            }
        } else {
            return "";
        }
    }

    public boolean getSuspendedStatus(String accountId) throws SQLException {
        if (accountExists(accountId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT suspended FROM accounts WHERE accountId = ?")) {
                preparedStatement.setString(1, accountId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getBoolean("suspended");
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public boolean isDeleted(String accountId) throws SQLException {
        if (accountExists(accountId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT deleted FROM accounts WHERE accountId = ?")) {
                preparedStatement.setString(1, accountId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getBoolean("deleted");
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public float getCreditLimit(String accountId) throws SQLException {
        String query = "SELECT creditLimit FROM accounts WHERE accountId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, accountId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getFloat("creditLimit");
            } else {
                throw new SQLException("Account not found");
            }
        }
    }

    public float getCreditPercent(String accountId) throws SQLException {
        String query = "SELECT creditPercent FROM accounts WHERE accountId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, accountId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getFloat("creditPercent");
            } else {
                throw new SQLException("Account not found");
            }
        }
    }

    public long getAccountCreatedTimestamp(String accountId) throws SQLException {
        String query = "SELECT accountCreatedTimestamp FROM accounts WHERE accountId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, accountId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("accountCreatedTimestamp");
            } else {
                throw new SQLException("Account not found");
            }
        }
    }

    public long getLastInterestCalculation(String accountId) throws SQLException {
        String query = "SELECT lastInterestCalculation FROM accounts WHERE accountId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, accountId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("lastInterestCalculation");
            } else {
                throw new SQLException("Account not found");
            }
        }
    }

    // get all method
    public Map<String, Object> getData(String accountId) throws SQLException {
        Map<String, Object> data = new HashMap<>();
    
        data.put("uuid", getOwner(accountId));
        data.put("displayName", getDisplayName(accountId));
        data.put("balance", getBalance(accountId));
        data.put("suspended", getSuspendedStatus(accountId));
        data.put("deleted", isDeleted(accountId));
        data.put("creditLimit", getCreditLimit(accountId));
        data.put("creditPercent", getCreditPercent(accountId));
        data.put("accountCreatedTimestamp", getAccountCreatedTimestamp(accountId));
        data.put("lastInterestCalculation", getLastInterestCalculation(accountId));
    
        return data;
    }

    public List<String> getAllAccounts() throws SQLException {
        String sql = "SELECT accountId FROM accounts";
        List<String> result = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(resultSet.getString("accountId"));
                }
            }
        }
        return result;
    }

    // access management
    public void addAccessToAccount(String accountId, String uuid) throws SQLException {
        String sql = "INSERT INTO account_access (accountId, userId) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, accountId);
            preparedStatement.setString(2, uuid);
            preparedStatement.executeUpdate();
        }
    }

    public boolean hasAccessToAccount(String accountId, String uuid) throws SQLException {
        String sql = "SELECT COUNT(*) FROM account_access WHERE accountId = ? AND userId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, accountId);
            preparedStatement.setString(2, uuid);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    public boolean isPersonal(String accountId) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE personalAccountId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, accountId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public void removeAccessFromAccount(String accountId, String userId) throws SQLException {
        String sql = "DELETE FROM account_access WHERE accountId = ? AND userId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, accountId);
            preparedStatement.setString(2, userId);
            preparedStatement.executeUpdate();
        }
    }

    public void removeAccessAllFromAccount(String accountId) throws SQLException {
        String sql = "DELETE FROM account_access WHERE account_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, accountId);
            preparedStatement.executeUpdate();
        }
    }

    public List<String> getAllUsersWithAccess(String accountId, String ownerId) throws SQLException {
        String sql = "SELECT userId FROM account_access WHERE accountId = ?";
        List<String> userIds = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, accountId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    userIds.add(resultSet.getString("userId"));
                }
            }
        }
        return userIds;
    }

    // private functions
    private String generateAccountId() {
        double rawId = Math.floor((Math.random() * (999999 - 100000) + 100000));
        String accountIdNumber = String.valueOf((int) rawId);
        try {
            if (!accountExists(accountIdNumber)) {
                return accountIdNumber;
            } else {
                return generateAccountId();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }
    };
}
