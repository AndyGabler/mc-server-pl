package com.gabler.huntersmc.commands;

import com.gabler.huntersmc.commands.util.TerritoryInputUtil;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.util.TerritoryException;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TerritoryClaimCommand implements CommandExecutor {

    private final TerritoryData territoryData;

    public TerritoryClaimCommand(TerritoryData aTerritoryData) {
        this.territoryData = aTerritoryData;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final String territoryName = TerritoryInputUtil.territoryNameFromArguments(args);

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cOnly players can secure territory.");
            return true;
        }
        if (((Player) sender).getLocation().getWorld().getEnvironment() != World.Environment.NORMAL) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cCan only claim territory in the overworld.");
            return true;
        }

        Chunk chunk = ((Player) sender).getLocation().getChunk();

        try {
            territoryData.claimTerritory(chunk.getX(), chunk.getZ(), ((Player) sender).getUniqueId().toString(), territoryName);
            territoryData.save();
        } catch (TerritoryException territoryException) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "c" + territoryException.getMessage());
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            sender.sendMessage(
                ChatColor.COLOR_CHAR +
                "4Unknown error occurred. Please report timestamp to admin: " +
                new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date())
            );
        }

        sender.sendMessage(ChatColor.COLOR_CHAR + "aSuccessfully claimed territory!");
        return true;
    }
}
