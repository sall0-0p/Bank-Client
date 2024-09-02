package com.bucketbank.modules;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

import org.bukkit.configuration.file.FileConfiguration;

import com.bucketbank.App;

public class DiscordLogger {
    App plugin = App.getPlugin();
    FileConfiguration config = plugin.getConfig();

    public void log(String webhookType, String content) {
        String url = config.getString("webhooks." + webhookType + ".url");
        String color = config.getString("webhooks." + webhookType + ".color");
        String title = config.getString("webhooks." + webhookType + ".title"); 

        sendWebhook(url, content, title, color);
    }

    public void logRaw(String webhookType, String payload) {
        String url = config.getString("webhooks." + webhookType + ".url");
        sendWebhookRaw(url, payload);
    }

    private void sendWebhookRaw(String webhookUrl, String payload) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            System.out.println("Webhook URL is not set for this message type.");
            return;
        }

        try {
            final HttpsURLConnection connection = (HttpsURLConnection) new URL(webhookUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux i686) Gecko/20071127 Firefox/2.0.0.11");
            connection.setDoOutput(true);
            try (final OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write((payload).getBytes(StandardCharsets.UTF_8));
            }
            connection.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendWebhook(String webhookUrl, String content, String title, String color) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            System.out.println("Webhook URL is not set for this message type.");
            return;
        }

        try {
            final HttpsURLConnection connection = (HttpsURLConnection) new URL(webhookUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux i686) Gecko/20071127 Firefox/2.0.0.11");
            connection.setDoOutput(true);
            try (final OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(("{\"content\":null,\"embeds\":[{\"title\":\"" + title + "\",\"description\":\"" + content + "\",\"color\":" + color + "}],\"attachments\":[]}").getBytes(StandardCharsets.UTF_8));
            }
            connection.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
