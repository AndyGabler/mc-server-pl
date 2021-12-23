package com.gabler.huntersmc.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HuntCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args == null || args.length != 1) {
            return false;
        }

        final String targetName = args[0];
        final Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cNo player with name " + targetName + ".");
            return true;
        }

        int blockX = target.getLocation().getBlockX();
        int blockY = target.getLocation().getBlockY();
        int blockZ = target.getLocation().getBlockZ();

        sender.sendMessage(
            ChatColor.COLOR_CHAR + "aFound " + target.getName() + "! They are at: " + ChatColor.COLOR_CHAR + "e" +
            blockX + ", " + blockY + ", " + blockZ
        );
        target.sendMessage(ChatColor.COLOR_CHAR + "cYour location has been revealed by " + sender.getName() + "!");
        return true;
    }
}
