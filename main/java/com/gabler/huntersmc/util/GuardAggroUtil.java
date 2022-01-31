package com.gabler.huntersmc.util;

import com.gabler.huntersmc.context.guard.GuardData;
import com.gabler.huntersmc.context.guard.model.Guard;
import com.gabler.huntersmc.context.relationship.RelationshipData;
import com.gabler.huntersmc.context.relationship.model.PlayerRelationship;
import com.gabler.huntersmc.context.relationship.model.RelationshipType;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.context.territory.model.Territory;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;

import java.util.Date;

public class GuardAggroUtil {

    public static boolean canGuardTarget(
        Entity guard,
        Entity target,
        TerritoryData territoryData,
        RelationshipData relationshipData,
        GuardData guardData
    ) {
        // If a guard lost aggro, oh well
        if (target == null) {
            return true;
        }

        // If a guard is standing on someone else's territory, it's useless
        final Chunk guardChunk = guard.getLocation().getChunk();
        final Guard guardInfo = guardData.guardForUuid(guard.getUniqueId().toString());
        final Territory guardCurrentTerritory = territoryData.getTerritoryFromChunk(guardChunk.getX(), guardChunk.getZ());
        if (guardCurrentTerritory != null && guardCurrentTerritory != guardInfo.getOwner()) {
            return false;
        }

        // Guards shall not attack their master nor their allies
        if (isGuardDiplomaticOrSubservient(target, guardInfo, territoryData, relationshipData)) {
            return false;
        }

        // Guards are willing to target, given that target is not on their own territory
        final Chunk targetChunk = target.getLocation().getChunk();
        final Territory targetHomeTerritory = territoryData.getTerritoryByOwnerUuid(target.getUniqueId().toString());
        final Territory targetCurrentTerritory = territoryData.getTerritoryFromChunk(targetChunk.getX(), targetChunk.getZ());
        if (targetCurrentTerritory != null && targetHomeTerritory == targetCurrentTerritory) {
            return false;
        }

        return true;
    }

    public static boolean isGuardDiplomaticOrSubservient(
        Entity target,
        Guard guard,
        TerritoryData territoryData,
        RelationshipData relationshipData
    ) {
        final Territory targetHomeTerritory = territoryData.getTerritoryByOwnerUuid(target.getUniqueId().toString());
        if (guard.getOwner() == targetHomeTerritory) {
            return true;
        }

        if (targetHomeTerritory == null) {
            return false;
        }

        final PlayerRelationship relationship = relationshipData.getTerritoryRelationship(targetHomeTerritory, guard.getOwner());
        if (
            relationship == null ||
            (relationship.getExpirationDate() != null && relationship.getExpirationDate().before(new Date()))
        ) {
            return false;
        }

        final RelationshipType relationshipType = relationship.getRelationshipType();
        if (relationshipType == RelationshipType.ALLY) {
            return true;
        }

        // If ambassador, the target cannot be the initiator; otherwise, they can envoy themselves into other territories
        return relationshipType == RelationshipType.AMBASSADOR && relationship.getInitiator() == guard.getOwner();
    }
}
