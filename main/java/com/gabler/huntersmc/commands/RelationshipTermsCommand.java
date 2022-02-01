package com.gabler.huntersmc.commands;

import com.gabler.huntersmc.commands.util.TerritoryInputUtil;
import com.gabler.huntersmc.context.relationship.RelationshipData;
import com.gabler.huntersmc.context.relationship.model.PlayerRelationship;
import com.gabler.huntersmc.context.relationship.model.RelationshipType;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.context.territory.model.Territory;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RelationshipTermsCommand implements CommandExecutor {

    private final TerritoryData territoryData;
    private final RelationshipData relationshipData;

    public RelationshipTermsCommand(TerritoryData aTerritoryData, RelationshipData aRelationshipData) {
        this.territoryData = aTerritoryData;
        this.relationshipData = aRelationshipData;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final String territoryName = TerritoryInputUtil.territoryNameFromArguments(args);

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cOnly players can interface with relationship data.");
            return true;
        }

        final Territory homeTerritory = territoryData.getTerritoryByOwnerUuid(((Player) sender).getUniqueId().toString());
        if (homeTerritory == null) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cYou must have a territory to have relationships with other territories.");
            return true;
        }

        List<PlayerRelationship> relationships;
        if (territoryName == null) {
            relationships = relationshipData.getTerritoryRelationships(homeTerritory).stream().filter(relationship ->
                relationship.getRelationshipType() != RelationshipType.NEUTRAL
            ).collect(Collectors.toList());
            if (relationships.isEmpty()) {
                sender.sendMessage(ChatColor.COLOR_CHAR + "aYou do not have any relationships with other territories.");
            }
        } else {
            final Territory territory = territoryData.getTerritoryByName(territoryName);
            if (territory == null) {
                sender.sendMessage(ChatColor.COLOR_CHAR + "cNo territory exists with name \"" + territoryName + "\".");
                return true;
            }
            final PlayerRelationship relationship = relationshipData.getTerritoryRelationship(territory, homeTerritory);
            if (relationship == null || relationship.getRelationshipType() == RelationshipType.NEUTRAL) {
                sender.sendMessage(ChatColor.COLOR_CHAR + "aYou have no relationship with " + territory.getName() + ".");
                return true;
            }
            relationships = Collections.singletonList(relationship);
        }

        relationships.forEach(relationship -> {
            final StringBuilder messageBuilder = new StringBuilder()
                .append(
                    ChatColor.COLOR_CHAR + "aYour relationship with " + getNameOfNotHomeTerritory(relationship, homeTerritory) +
                    " is: " + relationship.getRelationshipType().getSimpleName() + ChatColor.COLOR_CHAR + "a."
                );

            if (relationship.getExpirationDate() != null) {
                messageBuilder.append(
                    " (Expires " + ChatColor.COLOR_CHAR + "e" +
                    new SimpleDateFormat("yyyy-MM-dd HH:mm").format(relationship.getExpirationDate()) +
                    ChatColor.COLOR_CHAR + "a)"
                );
            }
            sender.sendMessage(messageBuilder.toString());
        });

        return true;
    }

    private String getNameOfNotHomeTerritory(PlayerRelationship relationship, Territory homeTerritory) {
        if (relationship.getTerritory1().getName().equalsIgnoreCase(homeTerritory.getName())) {
            return relationship.getTerritory2().getName();
        }

        return relationship.getTerritory1().getName();
    }
}
