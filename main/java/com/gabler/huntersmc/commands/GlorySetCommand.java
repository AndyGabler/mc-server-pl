package com.gabler.huntersmc.commands;

import com.gabler.huntersmc.context.glory.GloryData;
import com.gabler.huntersmc.util.uuid.UuidFetcher;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class GlorySetCommand implements CommandExecutor {

    private final GloryData gloryData;
    private final UuidFetcher uuidFetcher;

    public GlorySetCommand(GloryData aGloryData) {
        this(aGloryData, new UuidFetcher());
    }

    public GlorySetCommand(GloryData aGloryData, UuidFetcher anUuidFetcher) {
        gloryData = aGloryData;
        uuidFetcher = anUuidFetcher;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("huntersmc.set-glory") && !sender.isOp()) {
            return false;
        }

        if (args.length != 2) {
            return false;
        }

        final String playerName = args[0];
        final String amount = args[1];
        int gloryToSet;

        try {
            gloryToSet = Integer.parseInt(amount);
        } catch (Exception exception) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cCannot parse \"" + amount + "\" into glory amount.");
            return true;
        }

        UUID playerUuid;

        try {
            playerUuid = uuidFetcher.getUUID(playerName);
        } catch (Exception exception) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cError when calling external service.");
            exception.printStackTrace(); // TODO probably should attempt to log something but meh
            return true;
        }

        if (playerUuid == null) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cNo player found with name \"" + playerName + "\".");
            return true;
        }

        final String uuidString = playerUuid.toString();
        gloryData.registerProfile(uuidString);
        gloryData.hardSetPlayerGlory(uuidString, gloryToSet);

        try {
            gloryData.save();
        } catch (Exception exception) {
            exception.printStackTrace();
            sender.sendMessage(
                ChatColor.COLOR_CHAR + "4Unknown error occurred. Timestamp: " + new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date())
            );
        }

        sender.sendMessage(ChatColor.COLOR_CHAR + "aSuccessfully set glory amount for player.");
        return true;
    }
}
