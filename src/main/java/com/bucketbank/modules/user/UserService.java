package com.bucketbank.modules.user;

import com.bucketbank.modules.account.Account;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import com.bucketbank.Plugin;
import okhttp3.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UserService {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    private final String hostURL;
    private final String apiKey;

    public UserService() {
        Plugin plugin = Plugin.getPlugin();
        FileConfiguration config = plugin.getConfig();

        this.hostURL = config.getString("backend.host");
        this.apiKey = config.getString("backend.apiKey");
    }

    // create user
    public CompletableFuture<User> createUser(String username) {
        CompletableFuture<User> future = new CompletableFuture<>();
        String url = hostURL + "/user/" + username;

        Request request = new Request.Builder()
                .url(url)
                .header("X-API-KEY", apiKey)
                .post(RequestBody.create(gson.toJson(username), MediaType.parse("application/json")))
                .build();

        resolveUserFuture(future, request);

        return future;
    }

    // getters

    public CompletableFuture<User> getUserByIdAsync(UUID userId) throws IOException {
        CompletableFuture<User> future = new CompletableFuture<>();
        String url = hostURL + "/user/" + userId.toString();

        resolveUserFuture(future, userGET(future, url));
        return future;
    }

    public CompletableFuture<User> getUserByMinecraftUUIDAsync(UUID minecraftUUID) {
        CompletableFuture<User> future = new CompletableFuture<>();
        String url = hostURL + "/user/mc/" + minecraftUUID.toString();

        resolveUserFuture(future, userGET(future, url));
        return future;
    }

    public CompletableFuture<Boolean> userExistsAsync(UUID userId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        String url = hostURL + "/user/" + userId.toString();

        Request request = new Request.Builder()
                .url(url)
                .header("X-API-KEY", apiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() == 200) {
                    future.complete(true);
                } else if (response.code() == 404) {
                    future.complete(false);
                } else {
                    future.completeExceptionally(new IOException(String.format("%d: %s", response.code(), response.message())));
                }
            }
        });

        return future;
    }

    public CompletableFuture<Boolean> userExistsWithMinecraftUUIDAsync(UUID minecraftUUID) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        String url = hostURL + "/user/mc/" + minecraftUUID.toString();

        Request request = new Request.Builder()
                .url(url)
                .header("X-API-KEY", apiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() == 200) {
                    future.complete(true);
                } else if (response.code() == 404) {
                    future.complete(false);
                } else {
                    future.completeExceptionally(new IOException(String.format("%d: %s", response.code(), response.message())));
                }
            }
        });

        return future;
    }

    public CompletableFuture<User> updateUserAsync(UUID userId, Map<String, Object> patchData) {
        CompletableFuture<User> future = new CompletableFuture<>();
        String url = hostURL + "/user/" + userId.toString();

        Request request = new Request.Builder()
                .url(url)
                .header("X-API-KEY", apiKey)
                .patch(RequestBody.create(gson.toJson(patchData), MediaType.parse("application/json")))
                .build();

        resolveUserFuture(future, request);

        return future;
    }

    public void deleteUser(UUID userId) {
        String url = hostURL + "/user/" + userId.toString();

        new Request.Builder()
                .url(url)
                .header("X-API-KEY", apiKey)
                .delete()
                .build();
    }

    public CompletableFuture<List<Account>> getUserAccountsAsync(UUID userId) {
        CompletableFuture<List<Account>> future = new CompletableFuture<>();
        String url = hostURL + "/user/" + userId.toString();

        Request request = new Request.Builder()
                .url(url)
                .header("X-API-KEY", apiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String jsonResponse = response.body().string();

                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                    List<Map<String, Object>> accountsData = gson.fromJson(jsonResponse, listType);

                    List<Account> accounts = new ArrayList<>();
                    for (Map<String, Object> accountData : accountsData) {
                        if (accountData.containsKey("owner")) {
                            if (accountData.get("owner") instanceof Map) {
                                Map<String, Object> ownerData = jsonToMap(accountData.get("owner").toString());
                                accountData.put("owner", ownerData.get("id")); // Replace full owner object with owner.id
                            }
                        }

                        try {
                            Account account = new Account(accountData);
                            accounts.add(account);
                        } catch (Exception e) {
                            // I do not know what I will be doing here.
                            // Probably something

                            e.printStackTrace();
                        }
                    }

                    future.complete(accounts);
                } else {
                    future.completeExceptionally(new IOException(String.format("%d: %s", response.code(), response.message())));
                }
            }
        });

        return future;
    }

    // utility methods

    private Map<String, Object> jsonToMap(String json) {
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private Request userGET(CompletableFuture<User> future, String url) {
        return new Request.Builder()
                .url(url)
                .header("X-API-KEY", apiKey)
                .build();
    }

    private void resolveUserFuture(CompletableFuture<User> future, Request request) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    User user = new User(jsonToMap(response.body().string()));
                    future.complete(user);
                } else {
                    future.completeExceptionally(new IOException(String.format("%d: %s", response.code(), response.message())));
                }
            }
        });
    }
}
