package com.gabler.huntersmc.commands;

import com.gabler.huntersmc.commands.util.TerritoryInputUtil;
import com.gabler.huntersmc.context.glory.GloryData;
import com.gabler.huntersmc.context.relationship.RelationshipData;
import com.gabler.huntersmc.context.relationship.model.RelationshipType;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.context.territory.model.Territory;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class RelationshipEstablishCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final TerritoryData territoryData;
    private final RelationshipData relationshipData;
    private final RelationshipType relationshipType;
    private final GloryData gloryData;

    private static final ImmutableMap<RelationshipType, List<RelationshipType>> ACCEPTABLE_TRANSITIONS;
    static {
        ACCEPTABLE_TRANSITIONS = new ImmutableMap.Builder<RelationshipType, List<RelationshipType>>()
            .put(
                RelationshipType.NEUTRAL,
                Arrays.asList(RelationshipType.PENDING_ALLY, RelationshipType.WAR, RelationshipType.AMBASSADOR)
            )
            .put(
                // Negative case handled by rejection
                RelationshipType.PENDING_ALLY,
                Arrays.asList(RelationshipType.ALLY)
            )
            .build();
    }

    public RelationshipEstablishCommand(
        JavaPlugin aPlugin,
        TerritoryData aTerritoryData,
        RelationshipData aRelationshipData,
        GloryData aGloryData,
        RelationshipType aRelationshipType
    ) {
        this.plugin = aPlugin;
        this.territoryData = aTerritoryData;
        this.relationshipData = aRelationshipData;
        this.gloryData = aGloryData;
        this.relationshipType = aRelationshipType;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final boolean expirationDateRequired = relationshipType == RelationshipType.AMBASSADOR || relationshipType == RelationshipType.ALLY;
        String[] effectiveArgs = args;
        Date expirationDate = null;
        if (expirationDateRequired && args.length < 3) {
            return false;
        }

        if (expirationDateRequired) {
            effectiveArgs = new String[args.length - 2];
            System.arraycopy(args, 0, effectiveArgs, 0, effectiveArgs.length);
            expirationDate = parseExpirationDate(sender, args[args.length - 2], args[args.length - 1]);
            if (expirationDate == null) {
                return true;
            }
        }

        final String territoryName = TerritoryInputUtil.territoryNameFromArguments(effectiveArgs);
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

        if (targetTerritory.getOwnerUuid().equalsIgnoreCase(homeTerritory.getOwnerUuid())) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cYou own " + targetTerritory.getName() + ".");
            return true;
        }

        doTransitionLogic((Player) sender, homeTerritory, targetTerritory, expirationDate);
        return true;
    }

    private Date parseExpirationDate(CommandSender sender, String timeQuantityText, String timeUnitString) {
        int timeQuantity = 0;
        try {
            timeQuantity = Integer.parseInt(timeQuantityText);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cTime quantity of \"" + timeQuantityText + "\" is not a number.");
            return null;
        }

        int timeUnit = -1;
        if ("Day".equalsIgnoreCase(timeUnitString)) {
            timeUnit = Calendar.DAY_OF_YEAR;
        } else if ("Hour".equalsIgnoreCase(timeUnitString)) {
            timeUnit = Calendar.HOUR;
        } else if ("Minute".equalsIgnoreCase(timeUnitString)) {
            timeUnit = Calendar.MINUTE;
        } else {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cTime unit of \"" + timeUnitString + "\" must be either Day, Hour or Minute.");
            return null;
        }

        final Calendar expirationCalendar = Calendar.getInstance();
        expirationCalendar.setTime(new Date());
        expirationCalendar.add(timeUnit, timeQuantity);

        return expirationCalendar.getTime();
    }

    /**
     * Perform the transition of relationships between territories.
     *
     * @param initiator Initiator of the relationship
     * @param senderTerritory Sender's territory
     * @param targetTerritory Territory the command is targeting
     * @param expirationDate Expiration date
     * @return The new type of relationship
     */
    private void doTransitionLogic(
        Player initiator,
        Territory senderTerritory,
        Territory targetTerritory,
        Date expirationDate
    ) {
        final RelationshipType existingRelationshipType = relationshipData.getTerritoryRelationshipType(senderTerritory, targetTerritory);

        // First up, ensure that the transition is proper
        final List<RelationshipType> acceptableTransitions = ACCEPTABLE_TRANSITIONS.get(existingRelationshipType);
        // If this is an ally transition and the current state it neutral, we're really transitioning to Pending Alliance
        RelationshipType effectiveNewRelationship =
            relationshipType == RelationshipType.ALLY && existingRelationshipType == RelationshipType.NEUTRAL
                ? RelationshipType.PENDING_ALLY : relationshipType;
        if (acceptableTransitions == null || !acceptableTransitions.contains(effectiveNewRelationship)) {
            initiator.sendMessage(
                ChatColor.COLOR_CHAR + "cYou must end your " + existingRelationshipType.getSimpleName() + ChatColor.COLOR_CHAR +
                "c with " + targetTerritory.getName() + " before attempting to create a(n) " + relationshipType.getSimpleName() +
                ChatColor.COLOR_CHAR + "c with them."
            );
            return;
        }

        // Check glory prerequisites
        if (!validateGlory(initiator, senderTerritory.getOwnerUuid(), targetTerritory.getName(), targetTerritory.getOwnerUuid())) {
            return;
        }

        if (existingRelationshipType == RelationshipType.PENDING_ALLY) {
            boolean senderIsInitiator = relationshipData.getTerritoryRelationship(senderTerritory, targetTerritory).getInitiator().getOwnerUuid().equalsIgnoreCase(initiator.getUniqueId().toString());
            // If sender is the initiator of the pending alliance, do nothing. Other player has to accept it.
            if (senderIsInitiator) {
                initiator.sendMessage(ChatColor.COLOR_CHAR + "cYou have a pending alliance with " + targetTerritory.getName() + ". They can either accept or reject your alliance.");
                return;
            } else {
                // If initiator is the consenting party to an alliance, then all parties are good to go and the alliance can be established.
                effectiveNewRelationship = RelationshipType.ALLY;
            }

        }

        updateGloryCache(senderTerritory.getOwnerUuid(), effectiveNewRelationship, false);
        updateGloryCache(targetTerritory.getOwnerUuid(), effectiveNewRelationship, false);
        // Validations done. This relationship is good to go.
        try {
            relationshipData.setTerritoryRelationshipType(senderTerritory, targetTerritory, effectiveNewRelationship, expirationDate);
            relationshipData.save();
            gloryData.save();
        } catch (Exception exception) {
            exception.printStackTrace();
            initiator.sendMessage(
                ChatColor.COLOR_CHAR + "4Unknown error occurred. Please report timestamp to admin: " +
                new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date())
            );

            // Backout for the glory updates
            updateGloryCache(senderTerritory.getOwnerUuid(), effectiveNewRelationship, true);
            updateGloryCache(targetTerritory.getOwnerUuid(), effectiveNewRelationship, true);
        }

        final StringBuilder initiatorMessageBuilder = new StringBuilder()
            .append(ChatColor.COLOR_CHAR + "aYou now have a(n) " + effectiveNewRelationship.getSimpleName() + ChatColor.COLOR_CHAR + "a with ")
            .append(targetTerritory.getName() + ".");
        final StringBuilder recipientMessageBuilder = new StringBuilder()
            .append(ChatColor.COLOR_CHAR + "aYou now have a(n) " + effectiveNewRelationship.getSimpleName() + ChatColor.COLOR_CHAR + "a with ")
            .append(senderTerritory.getName() + ".");

        if (expirationDate != null) {
            final String formattedEndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(expirationDate);
            initiatorMessageBuilder.append(" This will end at " + formattedEndDate + ".");
            recipientMessageBuilder.append(" This will end at " + formattedEndDate + ".");
        }
        if (effectiveNewRelationship == RelationshipType.PENDING_ALLY) {
            recipientMessageBuilder.append(" You can approve or reject this by using the /ally or /rejectalliance command.");
        }

        initiator.sendMessage(initiatorMessageBuilder.toString());
        final Player recipient = Bukkit.getPlayer(UUID.fromString(targetTerritory.getOwnerUuid()));
        if (recipient != null) {
            recipient.sendMessage(recipientMessageBuilder.toString());
        }
    }

    private boolean validateGlory(Player initiator, String initiatingPlayerUuid, String recipientName, String recipientPlayerUuid) {
        if (relationshipType != RelationshipType.WAR) {
            return true;
        }

        final Integer recipientGloryAmount = gloryData.gloryAmountForPlayer(recipientPlayerUuid);
        if (recipientGloryAmount == null) {
            initiator.sendMessage(ChatColor.COLOR_CHAR + "cNo glory profile for target territory. Notify admin.");
            return false;
        }

        final int minimumCost = plugin.getConfig().getInt("glory-config.price.declare-war-min");
        final int maximumCost = plugin.getConfig().getInt("glory-config.price.declare-war-max");
        final int cost = Math.max(minimumCost, Math.min(recipientGloryAmount / 10, maximumCost));
        final Integer gloryAmount = gloryData.gloryAmountForPlayer(initiatingPlayerUuid);
        if (gloryAmount == null) {
            initiator.sendMessage(ChatColor.COLOR_CHAR + "cYou have no glory profile. Notify admin.");
            return false;
        } else if (gloryAmount < cost) {
            initiator.sendMessage(
                ChatColor.COLOR_CHAR + "cDeclaring war on " + recipientName + " requires " + cost + " glory. You only" +
                " have " + gloryAmount + "."
            );
            return false;
        } else {
            // If you have enough glory to start the war, do it
            gloryData.hardSetPlayerGlory(initiatingPlayerUuid, gloryAmount - cost);
        }

        return true;
    }

    private void updateGloryCache(String playerUuid, RelationshipType newRelationship, boolean backout) {
        if (newRelationship == RelationshipType.ALLY) {
            gloryData.changeAllyCount(playerUuid, !backout);
        } else if (newRelationship == RelationshipType.WAR) {
            gloryData.changeWarCount(playerUuid, !backout);
        }
    }
}
