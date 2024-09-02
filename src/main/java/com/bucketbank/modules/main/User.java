package com.bucketbank.modules.main;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.bucketbank.App;
import com.bucketbank.database.AccountsDatabase;
import com.bucketbank.database.UsersDatabase;
import com.bucketbank.modules.managers.DatabaseManager;

public class User {
    private static final App plugin = App.getPlugin();
    private static final Logger logger = plugin.getLogger();
    private static DatabaseManager databaseManager = plugin.getDatabaseManager();
    private static UsersDatabase usersDatabase = databaseManager.getUsersDatabase();
    private static AccountsDatabase accountsDatabase = databaseManager.getAccountsDatabase();

    private String userId;
    private String username;
    private long profileCreatedTimestamp;
    private String personalAccountId;
    private boolean suspended;
    private float debt;
    private float accountLimit;
    private boolean deleted;

    // constructor (load user)
    public User(String requestUserId) throws Exception {
        try {
            if (!usersDatabase.userExists(requestUserId)) {
                throw new Exception("Account does not exist!");
            }
            Map<String, Object> userData = usersDatabase.getData(requestUserId);
            userId = requestUserId;
            username = (String) userData.get("username");
            profileCreatedTimestamp = (long) userData.get("profileCreatedTimestamp");
            personalAccountId = (String) userData.get("personalAccountId");
            suspended = (Boolean) userData.get("suspended");
            debt = (float) userData.get("debt");
            accountLimit = (float) userData.get("accountLimit");
            deleted = (Boolean) userData.get("deleted");

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public User(OfflinePlayer player) throws Exception {
        String userId = player.getUniqueId().toString();

        try {
            if (!usersDatabase.userExists(userId)) {
                throw new Exception("User does not exist!");
            }
            Map<String, Object> userData = usersDatabase.getData(userId);
            this.userId = userId;
            this.username = (String) userData.get("username");
            this.profileCreatedTimestamp = (long) userData.get("profileCreatedTimestamp");
            this.personalAccountId = (String) userData.get("personalAccountId");
            this.suspended = (Boolean) userData.get("suspended");
            this.debt = (float) userData.get("debt");
            this.accountLimit = (float) userData.get("accountLimit");
            this.deleted = (Boolean) userData.get("deleted");

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    
    // constructor (create new user from id)
    public User(OfflinePlayer player, boolean createNewUser, float creditLimit, float creditPercent) throws Exception {
        String userId = player.getUniqueId().toString();
        String username = player.getName();

        if (!usersDatabase.userExists(userId)) {
            try {
                usersDatabase.createUser(userId, username);
    
                // assing other values
                this.userId = userId;
                this.username = username;
                this.profileCreatedTimestamp = usersDatabase.getCreationTimestamp(userId);
                this.suspended = usersDatabase.getSuspendedStatus(userId);
                this.debt = usersDatabase.getDebt(userId);
                this.accountLimit = usersDatabase.getAccountLimit(userId);
                this.deleted = usersDatabase.isDeleted(userId);

                // create personal account for user
                Account personalAccount = new Account(userId, true, creditLimit, creditPercent);
                personalAccount.setDisplayName(this.username);

                personalAccountId = personalAccount.getAccountId();
                usersDatabase.setPersonalAccountId(userId, personalAccountId);
    
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            throw new Exception("User already exists!");
        }
    }

    // getters

    public String getUsername() {
        return username;
    }

    public String getUserId() {
        return userId;
    }

    public Account getPersonalAccount() throws Exception {
        Account account = new Account(usersDatabase.getPersonalAccountId(userId));

        return account;
    }

    public String getPersonalAccountId() {
        try {
            personalAccountId = usersDatabase.getPersonalAccountId(userId);
            return personalAccountId;
        } catch (SQLException e) {
            logger.severe("Failed to get personal account id " + userId);
            e.printStackTrace();
            return "";
        }
    }

    public long getProfileCreatedTimestamp() {
        return profileCreatedTimestamp;
    }

    public boolean isDeleted() {
        try {
            deleted = usersDatabase.isDeleted(this.userId);
            return deleted;
        } catch (SQLException e) {
            logger.severe("Failed to check if user is deleted | " + userId);
            e.printStackTrace();
            return true;
        }
    }
    
    public boolean isSuspended() {
        try {
            suspended = usersDatabase.getSuspendedStatus(userId);
            return suspended;
        } catch (SQLException e) {
            logger.severe("Failed to check if user is suspended | " + userId);
            e.printStackTrace();
            return true;
        }
    }

    public void getDebt() {
        // later
    }

    public float getAccountLimit() {
        try {
            return usersDatabase.getAccountLimit(userId);
        } catch (SQLException e) {
            logger.severe("Failed to get account limit for user " + userId);
            e.printStackTrace();
            
            return 0;
        }
    }

    public List<String> getOwnedAccounts() {
        try {
            List<String> accountIds = usersDatabase.getAllOwnedAccounts(userId);
            return accountIds;
        } catch (SQLException e) {
            logger.severe("Failed to get accounts owned by user " + userId);
            e.printStackTrace();
            
            List<String> empty_list = new ArrayList<>();
            return empty_list;
        }
    }

    public List<String> getAccessibleAccounts() throws Exception {
        
        try {
            List<String> accountIds = usersDatabase.getAllAccessibleAccounts(userId);
            return accountIds;
        } catch (SQLException e) {
            logger.severe("Failed to get accessible accounts for user " + userId);
            e.printStackTrace();
            
            throw new Exception("Failed to get accessible accounts for user" + userId);
        }
    }
    
    // setters

    public void setPersonalAccount(Account account) {
        try {
            personalAccountId = account.getAccountId();
            usersDatabase.setPersonalAccountId(userId, personalAccountId);
        } catch (SQLException e) {
            logger.severe("Failed to set personal account for " + userId);
            e.printStackTrace();
        }
    }

    public void setPersonalAccountId(String accountId) {
        try {
            personalAccountId = accountId;
            usersDatabase.setPersonalAccountId(userId, accountId);
        } catch (SQLException e) {
            logger.severe("Failed to set personal account id for " + userId);
            e.printStackTrace();
        }
    }

    public void suspend() {
        try {
            usersDatabase.setSuspendedStatus(this.userId, true);
            this.suspended = true;
        } catch (SQLException e) {
            logger.severe("Failed to suspend user" + userId);
            e.printStackTrace();
        }
    }

    public void reinstate() {
        try {
            usersDatabase.setSuspendedStatus(this.userId, false);
            this.suspended = false;
        } catch (SQLException e) {
            logger.severe("Failed to reinstate user" + userId);
            e.printStackTrace();
        }
    }

    public void setAccountLimit(float newLimit) {
        try {
            usersDatabase.setAccountLimit(userId, newLimit);
        } catch (SQLException e) {
            logger.severe("Failed to set account limit for user " + userId);
            e.printStackTrace();
        }
    }

    public void pardonDebt() {
        // for future
    }

    // "delete" user
    public void deleteUser() throws Exception {
        try {
            List<String> ownedAccounts = getOwnedAccounts();

            for (String accountId : ownedAccounts) {
                accountsDatabase.deleteAccount(accountId);
            }

            usersDatabase.deleteUser(this.userId);
            this.deleted = true;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Failed to check delete user");
        }
    }

    // exists (by username)
    public static boolean existsWithUsername(String username) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(username);
        UUID userId = player.getUniqueId();

        try {
            return usersDatabase.userExists(userId.toString());
        } catch (SQLException e) {
            logger.severe("Failed to check if user exists with nickname " + username);
            e.printStackTrace();

            return false;
        }
        
    }
}
