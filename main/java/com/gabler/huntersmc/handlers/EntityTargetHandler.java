package com.gabler.huntersmc.handlers;

import com.gabler.huntersmc.context.guard.GuardData;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class EntityTargetHandler implements Listener {

    private final GuardData guardData;

    public EntityTargetHandler(GuardData aGuardData) {
        this.guardData = aGuardData;
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent targetEvent) {

        // TODO do some checking here for team and if it is a player
        if (isGuard(targetEvent.getEntity()) || isGuard(targetEvent.getTarget())) {
            targetEvent.setCancelled(true);
        }
    }

    private boolean isGuard(Entity entity) {
        return entity != null && guardData.entityIsGuard(entity.getUniqueId().toString());
    }
}
