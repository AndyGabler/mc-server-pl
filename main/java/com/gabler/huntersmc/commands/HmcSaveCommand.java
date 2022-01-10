package com.gabler.huntersmc.commands;

import com.gabler.huntersmc.HuntersMcPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HmcSaveCommand implements CommandExecutor {

    private final HuntersMcPlugin plugin;

    public HmcSaveCommand(HuntersMcPlugin aPlugin) {
        plugin = aPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("huntersmc.forcesave") || sender.isOp()) {
            plugin.getLogger().info("A force save of HuntersMC data has been iniated by " + sender.getName() + ". Save attempt starting...");
            sender.sendMessage(ChatColor.COLOR_CHAR + "bForce save initiating...");

            try {
                plugin.doSave();
                sender.sendMessage(ChatColor.COLOR_CHAR + "aForce save of HuntersMC data successful.");
            } catch (Exception exception) {
                plugin.getLogger().severe("Failed to save HuntersMC data ");
                exception.printStackTrace();
                sender.sendMessage(ChatColor.COLOR_CHAR + "4Save of HuntersMC data failed. See logs.");
            }

            return true;
        }
        return false;
    }
}
