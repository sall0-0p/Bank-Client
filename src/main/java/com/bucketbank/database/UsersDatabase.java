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
import java.util.logging.Logger;

import com.bucketbank.App;
import com.bucketbank.modules.managers.DatabaseManager;

public class UsersDatabase {
    private final Connection connection;
    private static final App plugin = App.getPlugin();
    private static DatabaseManager databaseManager = App.getDatabaseManager();
    private static final Logger logger = plugin.getLogger();

    public UsersDatabase(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        // Users table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS users (" +
                "userId TEXT NOT NULL PRIMARY KEY, " +
                "username TEXT NOT NULL, " +
                "profileCreatedTimestamp BIGINT NOT NULL, " +
                "personalAccountId TEXT DEFAULT 000000, " +
                "suspended BOOL DEFAULT 0, " +
                "debt FLOAT DEFAULT 0, " +
                "accountLimit FLOAT DEFAULT 3, " +
                "deleted BOOL DEFAULT 0" +
            ")");
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    };

    public void createUser(String userId, String username) throws SQLException {
        String sql = "INSERT INTO users (userId, username, profileCreatedTimestamp) VALUES (?, ?, ?)";

        if (!userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                long unixTimestamp = (System.currentTimeMillis() / 1000L);

                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, username);
                preparedStatement.setLong(3, unixTimestamp);

                preparedStatement.executeUpdate();
            }
        }
    }

    public void deleteUser(String userId) throws SQLException {
        String sql = "UPDATE users SET deleted = ? WHERE userId = ?";

        if (userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setBoolean(1, true);
                preparedStatement.setString(2, userId);
                preparedStatement.executeUpdate();
            }
        }
    }

    public boolean userExists(String userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE userId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            boolean result = resultSet.next();
            return result;
        }
    }

    // setters
    public void setPersonalAccountId(String userId, String accountId) throws SQLException {
        String sql = "UPDATE users SET personalAccountId = ? WHERE userId = ?";
        if (userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, accountId);
                preparedStatement.setString(2, userId);
                preparedStatement.executeUpdate();
            }
        }
    }

    public void setSuspendedStatus(String userId, boolean freezed) throws SQLException {
        String sql = "UPDATE users SET suspended = ? WHERE userId = ?";
        if (userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setBoolean(1, freezed);
                preparedStatement.setString(2, userId);
                preparedStatement.executeUpdate();
            }
        }
    }

    // excludes loans and credit card debt!
    public void setDebt(String userId, float debtAmount) throws SQLException {
        String sql = "UPDATE users SET debt = ? WHERE userId = ?";
        if (userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setFloat(1, debtAmount);
                preparedStatement.setString(2, userId);
                preparedStatement.executeUpdate();
            }
        }
    }

    public void setAccountLimit(String userId, float newLimit) throws SQLException {
        String sql = "UPDATE users SET accountLimit = ? WHERE userId = ?";
        if (userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setFloat(1, newLimit);
                preparedStatement.setString(2, userId);
                preparedStatement.executeUpdate();
            }
        }
    }

    // getters
    public String getUsername(String userId) throws SQLException {
        String sql = "SELECT username FROM users WHERE userId = ?";
        if (userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getString("username");
                } else {
                    return "";
                }
            }
        } else {
            return "";
        }
    }

    public long getCreationTimestamp(String userId) throws SQLException {
        String sql = "SELECT profileCreatedTimestamp FROM users WHERE userId = ?";

        if (userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getLong("profileCreatedTimestamp");
                } else {
                    return (long) 0;
                }
            }
        } else {
            return (long) 0;
        }
    }

    public String getPersonalAccountId(String userId) throws SQLException {
        String sql = "SELECT personalAccountId FROM users WHERE userId = ?";

        if (userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getString("personalAccountId");
                } else {
                    return "000000";
                }
            }
        } else {
            return "000000";
        }
    }

    public boolean getSuspendedStatus(String userId) throws SQLException {
        String sql = "SELECT suspended FROM users WHERE userId = ?";
        if (userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userId);
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

    public float getDebt(String userId) throws SQLException {
        String sql = "SELECT debt FROM users WHERE userId = ?";
        if (userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getFloat("debt");
                } else {
                    return 0;
                }
            }
        } else {
            return 0;
        }
    }

    public float getAccountLimit(String userId) throws SQLException {
        String sql = "SELECT accountLimit FROM users WHERE userId = ?";
        if (userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getInt("accountLimit");
                } else {
                    return 0;
                }
            }
        } else {
            return 0;
        }
    }

    public boolean isDeleted(String userId) throws SQLException {
        String sql = "SELECT deleted FROM users WHERE userId = ?";
        if (userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userId);
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

    public List<String> getAllAccessibleAccounts(String userId) throws SQLException {
        String sql = "SELECT accountId FROM account_access WHERE userId = ?";
        List<String> accountIds = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    accountIds.add(resultSet.getString("accountId"));
                }
            }
        }
        return accountIds;
    }

    public List<String> getAllOwnedAccounts(String userId) throws SQLException {
        String sql = "SELECT accountId FROM accounts WHERE ownerId = ?";
        List<String> accountIds = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    accountIds.add(resultSet.getString("accountId"));
                }
            }
        }
        return accountIds;
    }

    // get all
    public Map<String, Object> getData(String userId) throws SQLException {
        Map<String, Object> data = new HashMap<>();

        data.put("uuid", userId);
        data.put("username", getUsername(userId));
        data.put("profileCreatedTimestamp", getCreationTimestamp(userId));
        data.put("suspended", getSuspendedStatus(userId));
        data.put("personalAccountId", getPersonalAccountId(userId));
        data.put("debt", getDebt(userId));
        data.put("accountLimit", getAccountLimit(userId));
        data.put("deleted", isDeleted(userId));

        return data;
    }

}
