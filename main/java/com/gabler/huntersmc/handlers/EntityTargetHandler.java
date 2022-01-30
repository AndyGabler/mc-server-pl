package com.gabler.huntersmc.handlers;

import com.gabler.huntersmc.context.guard.GuardData;
import com.gabler.huntersmc.context.relationship.RelationshipData;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.util.GuardAggroUtil;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class EntityTargetHandler implements Listener {

    private final TerritoryData territoryData;
    private final GuardData guardData;
    private final RelationshipData relationshipData;

    public EntityTargetHandler(TerritoryData aTerritoryData, GuardData aGuardData, RelationshipData aRelationshipData) {
        this.territoryData = aTerritoryData;
        this.guardData = aGuardData;
        this.relationshipData = aRelationshipData;
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent targetEvent) {

        // Guards cannot be targeted
        if (isGuard(targetEvent.getTarget())) {
            targetEvent.setCancelled(true);
        }

        // If the entity is a guard, there is other criteria that must be met
        if (
            isGuard(targetEvent.getEntity()) &&
            !GuardAggroUtil.canGuardTarget(targetEvent.getEntity(), targetEvent.getTarget(), territoryData, relationshipData, guardData)
        ) {
            targetEvent.setCancelled(true);
        }
    }

    private boolean isGuard(Entity entity) {
        return entity != null && guardData.entityIsGuard(entity.getUniqueId().toString());
    }
}
