package com.gabler.huntersmc.handlers;

import com.gabler.huntersmc.context.guard.GuardData;
import com.gabler.huntersmc.context.guard.model.Guard;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathHandler implements Listener {

    private final GuardData guardData;

    public EntityDeathHandler(GuardData aGuardData) {
        guardData = aGuardData;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent deathEvent) {
        final String entityUuid = deathEvent.getEntity().getUniqueId().toString();
        final Guard guard = guardData.guardForUuid(entityUuid);
        if (guard != null) {
            guardData.deleteGuard(guard);
        }
    }
}
