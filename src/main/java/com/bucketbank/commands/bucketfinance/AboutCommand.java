package com.bucketbank.commands.bucketfinance;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;

import com.bucketbank.App;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class AboutCommand implements Command {
    private static final App plugin = App.getPlugin();
    private static final Logger logger = plugin.getLogger();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private Map<String, String> placeholders = new HashMap<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            if (!sender.hasPermission("bucketfinance.about")) {
                throw new Exception("You do not have permission to use this command!");
            }

            String initialMessage;
            if (sender.hasPermission("bucketfinance.about.admin")) {
                initialMessage = Messages.getString("about_extended");
            } else {
                initialMessage = Messages.getString("about");
            }
                
            DecimalFormat df = new DecimalFormat(plugin.getConfig().getString("decimal_format"));
            df.setRoundingMode(RoundingMode.FLOOR);
            placeholders.put("%diamonds_in_economy%", String.valueOf(plugin.diamondsInEconomy));
            placeholders.put("%currency_in_economy%", String.valueOf(plugin.currencyInEconomy));
            placeholders.put("%exchange_course%", df.format(plugin.diamondsInEconomy / (float) plugin.getConfig().getInt("data.exchange_coefficient")));

            Component parsed = mm.deserialize(parsePlaceholders(initialMessage, placeholders));
            sender.sendMessage(parsed);
            } catch (Exception e) {
                sender.sendMessage(mm.deserialize("<red>| " + e.getMessage()));
            }
    }

    private static String parsePlaceholders(String input, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            input = input.replace(entry.getKey(), entry.getValue());
        }
        return input;
    }
}
