package com.bucketbank.modules.transaction;

import com.bucketbank.Plugin;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TransactionService {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    private final String hostURL;
    private final String apiKey;

    public TransactionService() {
        Plugin plugin = Plugin.getPlugin();
        FileConfiguration config = plugin.getConfig();

        this.hostURL = config.getString("backend.host");
        this.apiKey = config.getString("backend.apiKey");
    }

    public CompletableFuture<Transaction> createTransaction(String sourceAccountId, String destinationAccountId, float amount, String description) {
        CompletableFuture<Transaction> future = new CompletableFuture<>();
        String url = hostURL + "/transaction";

        Map<String, Object> body = new HashMap<>();
        body.put("sourceAccountId", sourceAccountId);
        body.put("destinationAccountId", destinationAccountId);
        body.put("amount", amount);
        body.put("description", description);

        Request request = new Request.Builder()
                .url(url)
                .header("X-API-KEY", apiKey)
                .post(RequestBody.create(gson.toJson(body), MediaType.parse("application/json")))
                .build();

        resolveTransactionFuture(future, request);
        return future;
    }

    public CompletableFuture<Transaction> getTransactionById(String transactionId) {
        CompletableFuture<Transaction> future = new CompletableFuture<>();
        String url = hostURL + "/transaction/" + transactionId;

        Request request = new Request.Builder()
                .url(url)
                .header("X-API-KEY", apiKey)
                .get()
                .build();

        resolveTransactionFuture(future, request);
        return future;
    }

    // utility methods

    private Map<String, Object> jsonToMap(String json) {
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void resolveTransactionFuture(CompletableFuture<Transaction> future, Request request) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    Transaction transaction = new Transaction(jsonToMap(response.body().string()));
                    future.complete(transaction);
                } else {
                    future.completeExceptionally(new IOException(String.format("%d: %s", response.code(), response.message())));
                }
            }
        });
    }
}
