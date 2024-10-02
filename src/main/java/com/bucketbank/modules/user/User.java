package com.bucketbank.modules.user;

import com.bucketbank.Plugin;
import com.google.gson.Gson;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class User {
    private final UserService userService = Plugin.getUserService();

    // values
    private final UUID id;
    private final UUID minecraftUUID;
    private final String username;

    private final boolean deleted;
    private boolean suspended;
    private int accountLimit;

    private String personalAccountId;

    private final Date createdAt;
    private final Date updatedAt;

    // constructor from JSON
    public User(Map<String, Object> data) {
        this.id = UUID.fromString(data.get("id").toString());
        this.minecraftUUID = UUID.fromString(data.get("minecraftUUID").toString());
        this.username = data.get("username").toString();

        this.suspended = data.get("suspended").toString().equals("true");
        this.deleted = data.get("deleted").toString().equals("true");

        this.accountLimit = Integer.parseInt(data.get("accountLimit").toString());
        this.personalAccountId = data.get("personalAccountId").toString();
        this.createdAt = (Date) data.get("createdAt");
        this.updatedAt = (Date) data.get("updatedAt");
    }

    public UUID getId() {
        return id;
    }

    public UUID getMinecraftUUID() {
        return minecraftUUID;
    }

    public String getUsername() {
        return username;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public int getAccountLimit() {
        return accountLimit;
    }

    public String getPersonalAccountId() {
        return personalAccountId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    // Setters

    public void setSuspended(boolean suspended) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("suspended", suspended);
        userService.updateUserAsync(this.id, jsonMap);
        this.suspended = suspended;
    }

    public void setPersonalAccountId(String personalAccountId) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("personalAccountId", personalAccountId);
        userService.updateUserAsync(this.id, jsonMap);
        this.personalAccountId = personalAccountId;
    }

    public void setAccountLimit(int accountLimit) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("accountLimit", accountLimit);
        userService.updateUserAsync(this.id, jsonMap);
        this.accountLimit = accountLimit;
    }
}
