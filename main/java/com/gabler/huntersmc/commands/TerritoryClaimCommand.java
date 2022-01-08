package com.gabler.huntersmc.commands;

import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.util.TerritoryException;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
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
        String territoryName = null;
        if (args.length > 0) {
            territoryName = "";
            for (int index = 0; index < args.length; index++) {
                if (index != 0) {
                    territoryName += " ";
                }

                territoryName += args[index];
            }
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cOnly players can secure territory.");
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
