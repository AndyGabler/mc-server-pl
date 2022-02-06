package com.gabler.huntersmc.commands;

import com.gabler.huntersmc.context.glory.GloryData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GloryCheckCommand implements CommandExecutor {

    private final GloryData gloryData;

    public GloryCheckCommand(GloryData aGloryData) {
        this.gloryData = aGloryData;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cMust be a players can have a glory profile.");
            return true;
        }

        final Integer gloryAmount = gloryData.gloryAmountForPlayer(((Player) sender).getUniqueId().toString());
        if (gloryAmount != null) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "aYour glory: " + ChatColor.COLOR_CHAR + "e" + gloryAmount + ChatColor.COLOR_CHAR + "a.");
        } else {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cYou have no glory profile. Notify admin.");
        }
        return true;
    }
}
