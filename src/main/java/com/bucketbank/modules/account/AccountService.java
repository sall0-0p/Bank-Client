package com.bucketbank.modules.account;

import com.bucketbank.Plugin;
import com.bucketbank.modules.transaction.Transaction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class AccountService {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    private final String hostURL;
    private final String apiKey;

    public AccountService() {
        Plugin plugin = Plugin.getPlugin();
        FileConfiguration config = plugin.getConfig();

        this.hostURL = config.getString("backend.host");
        this.apiKey = config.getString("backend.apiKey");
    }

    // create account
    public CompletableFuture<Account> createAccount(String userId) {
        CompletableFuture<Account> future = new CompletableFuture<>();
        String url = this.hostURL + "/account/" + userId;

        Request request = new Request.Builder()
                .url(url)
                .header("X-API-KEY", apiKey)
                .post(RequestBody.create(gson.toJson(userId), MediaType.parse("application/json")))
                .build();

        resolveAccountFuture(future, request);

        return future;
    }

    // get account by id
    public CompletableFuture<Account> getAccountAsync(String accountId) {
        CompletableFuture<Account> future = new CompletableFuture<>();
        String url = this.hostURL + "/account/" + accountId;

        Request request = new Request.Builder()
                .url(url)
                .header("X-API-KEY", this.apiKey)
                .build();

        resolveAccountFuture(future, request);

        return future;
    }

    public CompletableFuture<Boolean> accountExistsAsync(String accountId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        String url = this.hostURL + "/account/" + accountId;

        Request request = new Request.Builder()
                .url(url)
                .header("X-API-KEY", this.apiKey)
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
                    future.completeExceptionally(new IOException(String.format("%s : %s", response.code(), response.message())));
                }
            }
        });

        return future;
    }

    public CompletableFuture<Account> updateAccountAsync(String accountId, Map<String, Object> patchData) {
        CompletableFuture<Account> future = new CompletableFuture<>();
        String url = this.hostURL + "/account/" + accountId;

        Request request = new Request.Builder()
                .url(url)
                .header("X-API-KEY", this.apiKey)
                .patch(RequestBody.create(gson.toJson(patchData), MediaType.parse("application/json")))
                .build();

        resolveAccountFuture(future, request);
        return future;
    }

    public CompletableFuture<List<Transaction>> getAccountTransactionsAsync(String accountId) {
        CompletableFuture<List<Transaction>> future = new CompletableFuture<>();
        String url = this.hostURL + "/account/" + accountId + "/transactions";

        Request request = new Request.Builder()
                .url(url)
                .header("X-API-KEY", this.apiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    // Complete the future with an exception if the response is unsuccessful
                    future.completeExceptionally(new IOException("Unexpected code " + response));
                    return;
                }

                // Parse the JSON response using Gson
                try (ResponseBody responseBody = response.body()) {
                    if (responseBody != null) {
                        // Get the response as a string
                        String responseBodyString = responseBody.string();

                        // Use Gson to parse the response into a list of Transaction objects
                        Gson gson = new Gson();
                        Type transactionListType = new TypeToken<List<Transaction>>(){}.getType();
                        List<Transaction> transactions = gson.fromJson(responseBodyString, transactionListType);

                        // Complete the future with the transaction list
                        future.complete(transactions);
                    } else {
                        future.completeExceptionally(new IOException("Response body is null"));
                    }
                } catch (Exception e) {
                    future.completeExceptionally(e); // Complete with exception if parsing fails
                }
            }
        });

        return future;
    }

    public void deleteAccount(String accountId) {
        String url = hostURL + "/account/" + accountId;

        new Request.Builder()
                .url(url)
                .header("X-API-KEY", this.apiKey)
                .delete()
                .build();
    }

    // Utility methods

    private Map<String, Object> jsonToMap(String json) {
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void resolveAccountFuture(CompletableFuture<Account> future, Request request) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    try {
                        Account account = new Account(jsonToMap(response.body().string()));
                        future.complete(account);
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }

                } else {
                    future.completeExceptionally(new IOException(String.format("%d: %s", response.code(), response.message())));
                }
            }
        });
    }
}

