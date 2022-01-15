package com.gabler.huntersmc.util;

import com.gabler.huntersmc.context.guard.GuardData;
import com.gabler.huntersmc.context.guard.model.Guard;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.context.territory.model.Territory;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;

public class GuardAggroUtil {

    public static boolean canGuardTarget(
        Entity guard,
        Entity target,
        TerritoryData territoryData,
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

        // Guards shall not attack their master
        final Territory targetHomeTerritory = territoryData.getTerritoryByOwnerUuid(target.getUniqueId().toString());
        if (guardInfo.getOwner() == targetHomeTerritory) {
            return false;
        }

        // Guards are willing to target, given that target is not on their own territory
        final Chunk targetChunk = target.getLocation().getChunk();
        final Territory targetCurrentTerritory = territoryData.getTerritoryFromChunk(targetChunk.getX(), targetChunk.getZ());
        if (targetCurrentTerritory != null && targetHomeTerritory == targetCurrentTerritory) {
            return false;
        }

        return true;
    }
}
