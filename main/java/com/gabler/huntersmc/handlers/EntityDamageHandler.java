package com.gabler.huntersmc.handlers;

import com.gabler.huntersmc.context.guard.GuardData;
import com.gabler.huntersmc.context.guard.model.Guard;
import com.gabler.huntersmc.context.relationship.RelationshipData;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.util.GuardAggroUtil;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageHandler implements Listener {

    private final TerritoryData territoryData;
    private final GuardData guardData;
    private final RelationshipData relationshipData;

    public EntityDamageHandler(TerritoryData aTerritoryData, GuardData aGuardData, RelationshipData aRelationshipData) {
        this.territoryData = aTerritoryData;
        this.guardData = aGuardData;
        this.relationshipData = aRelationshipData;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // First, check if the entity is a guard
        final Guard guard = guardData.guardForUuid(event.getEntity().getUniqueId().toString());
        if (guard != null) {
            if (!guard.getOwner().getOwnerUuid().equalsIgnoreCase(event.getDamager().getUniqueId().toString())) {
                if (GuardAggroUtil.isGuardDiplomaticOrSubservient(event.getDamager(), guard, territoryData, relationshipData)) {
                    event.getDamager().sendMessage(
                        ChatColor.COLOR_CHAR + "cYou have a diplomatic relationship with " + guard.getOwner().getName() +
                        ". Maybe not attack the guards?"
                    );
                    event.setCancelled(true);
                }
            }
        }
    }
}
