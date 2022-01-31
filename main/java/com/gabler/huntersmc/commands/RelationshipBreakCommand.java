package com.gabler.huntersmc.commands;

import com.gabler.huntersmc.commands.util.TerritoryInputUtil;
import com.gabler.huntersmc.context.relationship.RelationshipData;
import com.gabler.huntersmc.context.relationship.model.RelationshipType;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.context.territory.model.Territory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class RelationshipBreakCommand implements CommandExecutor {

    private final TerritoryData territoryData;
    private final RelationshipData relationshipData;
    private final RelationshipType relationshipType;

    public RelationshipBreakCommand(
        TerritoryData aTerritoryData,
        RelationshipData aRelationshipData,
        RelationshipType aRelationshipType
    ) {
        this.territoryData = aTerritoryData;
        this.relationshipData = aRelationshipData;
        this.relationshipType = aRelationshipType;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final String territoryName = TerritoryInputUtil.territoryNameFromArguments(args);

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cOnly players can interface with relationship data.");
            return true;
        }

        if (territoryName == null) {
            return false;
        }

        final Territory homeTerritory = territoryData.getTerritoryByOwnerUuid(((Player) sender).getUniqueId().toString());
        if (homeTerritory == null) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cYou must have a territory to build/sever relationships with other territories.");
            return true;
        }

        final Territory targetTerritory = territoryData.getTerritoryByName(territoryName);
        if (targetTerritory == null) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cNo territory exists with name \"" + territoryName + "\".");
            return true;
        }

        final RelationshipType relationshipType = relationshipData.getTerritoryRelationshipType(homeTerritory, targetTerritory);
        if (relationshipType != this.relationshipType) {
            sender.sendMessage(
                ChatColor.COLOR_CHAR + "cYour relationship with " + territoryName + " is " + relationshipType.getSimpleName() +
                " not " + this.relationshipType.getSimpleName() + "."
            );
            return true;
        }
        
        try {
            relationshipData.setTerritoryRelationshipType(homeTerritory, targetTerritory, RelationshipType.NEUTRAL, null);
            relationshipData.save();
        } catch (Exception exception) {
            exception.printStackTrace();
            sender.sendMessage(
               ChatColor.COLOR_CHAR + "4Unknown error occurred. Please report timestamp to admin: " +
               new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date())
            );
        }

        sender.sendMessage(
            ChatColor.COLOR_CHAR + "aYour " + relationshipType.getSimpleName() + " with " + targetTerritory.getName() +
            " has ended. Returning to neutral terms."
        );
        final Player recipient = Bukkit.getPlayer(UUID.fromString(targetTerritory.getOwnerUuid()));
        if (recipient != null) {
            recipient.sendMessage(
                ChatColor.COLOR_CHAR + "aYour " + relationshipType.getSimpleName() + " with " + homeTerritory.getName() +
                " has ended. Returning to neutral terms."
            );
        }
        return true;
    }
}
