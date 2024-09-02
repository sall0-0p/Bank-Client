package com.bucketbank.modules;

import org.bukkit.command.CommandSender;

public interface Command {
    void execute(CommandSender sender, String[] args);
}
