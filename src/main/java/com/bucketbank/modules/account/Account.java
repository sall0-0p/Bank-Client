package com.bucketbank.modules.account;

import com.bucketbank.Plugin;
import com.bucketbank.modules.user.User;
import com.bucketbank.modules.user.UserService;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class Account {
    private final AccountService accountService = Plugin.getAccountService();
    private static final UserService userService = Plugin.getUserService();
    private static final Logger logger = Plugin.getStaticLogger();

    // values
    private final String id;
    private User owner;
    private String displayName;

    private float balance;

    private final boolean deleted;
    private boolean suspended;

    private float creditLimit;
    private float creditPercent;

    private final Date createdAt;
    private final Date updatedAt;


    // constructor from JSON
    public Account(Map<String, Object> data) throws Exception {
        this.id = (String) data.get("id");
        this.displayName = (String) data.get("displayName");
        this.balance = Float.parseFloat((String) data.get("balance"));
        this.deleted = Boolean.parseBoolean((String) data.get("deleted"));
        this.suspended = Boolean.parseBoolean((String) data.get("suspended"));
        this.creditLimit = Float.parseFloat((String) data.get("creditLimit"));
        this.creditPercent = Float.parseFloat((String) data.get("creditPercent"));
        this.createdAt = Date.from(ZonedDateTime.parse((String) data.get("createdAt")).toInstant());
        this.updatedAt = Date.from(ZonedDateTime.parse((String) data.get("updatedAt")).toInstant());

        CompletableFuture<User> future = userService.getUserByIdAsync(UUID.fromString((String) data.get("id")));

        future.thenAccept(result -> {
            if (result != null) {
                this.owner = result;
            }
        });

        if (this.owner == null) {
            throw new Exception("Owner cannot be null!");
        }
    }

    public String getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("displayName", displayName);
        accountService.updateAccountAsync(this.id, jsonMap);
        this.displayName = displayName;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("balance", balance);
        accountService.updateAccountAsync(this.id, jsonMap);
        this.balance = balance;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("suspended", suspended);
        accountService.updateAccountAsync(this.id, jsonMap);
        this.suspended = suspended;
    }

    public float getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(float creditLimit) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("creditLimit", creditLimit);
        accountService.updateAccountAsync(this.id, jsonMap);
        this.creditLimit = creditLimit;
    }

    public float getCreditPercent() {
        return creditPercent;
    }

    public void setCreditPercent(float creditPercent) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("creditPercent", creditPercent);
        accountService.updateAccountAsync(this.id, jsonMap);
        this.creditPercent = creditPercent;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
}
