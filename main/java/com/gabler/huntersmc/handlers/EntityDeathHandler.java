package com.gabler.huntersmc.handlers;

import com.gabler.huntersmc.context.glory.GloryData;
import com.gabler.huntersmc.context.guard.GuardData;
import com.gabler.huntersmc.context.guard.model.Guard;
import com.gabler.huntersmc.util.TransactionUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EntityDeathHandler implements Listener {

    private final JavaPlugin plugin;
    private final GuardData guardData;
    private final GloryData gloryData;

    public EntityDeathHandler(JavaPlugin aPlugin, GuardData aGuardData, GloryData aGloryData) {
        plugin = aPlugin;
        guardData = aGuardData;
        gloryData = aGloryData;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent deathEvent) {
        final String logHeader = TransactionUtil.rollupTransactionIdLogHeader();

        final String entityUuid = deathEvent.getEntity().getUniqueId().toString();
        final Guard guard = guardData.guardForUuid(entityUuid);
        boolean gloryDelta = false;
        if (guard != null) {
            plugin.getLogger().info(logHeader + "Detected death of a guard with ID " + guard.getId() + ".");
            guardData.deleteGuard(guard);

            // Did someone kill the guard?
            final EntityDamageEvent damageEvent = deathEvent.getEntity().getLastDamageCause();
            if (damageEvent instanceof EntityDamageByEntityEvent) {
                final Entity damager = ((EntityDamageByEntityEvent) damageEvent).getDamager();
                if (damager instanceof Player) {
                    plugin.getLogger().info(logHeader + "Determined guard with ID " + guard.getId() + " killed by player.");

                    // Okay someone killed a guard... did their master kill them?
                    final String uuid = damager.getUniqueId().toString();
                    if (guard.getOwner().getOwnerUuid().equalsIgnoreCase(uuid)) {
                        plugin.getLogger().info(logHeader + "Determined guard with ID " + guard.getId() + " killed by their master.");
                        gloryData.applyGloryEvent(uuid, "Guard Murder", "glory-config.events.guard-execute-penalty");
                    } else {
                        plugin.getLogger().info(logHeader + "Determined guard with ID " + guard.getId() + " killed by player with UUID " + uuid + ".");
                        gloryData.applyGloryEvent(
                            uuid, "Guard Kill", "glory-config.events.kill-guard",
                            guard.getType().getSimpleName().toLowerCase()
                        );
                    }
                    gloryDelta = true;
                }
            }
        } else {
            if (
                deathEvent.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent &&
                ((EntityDamageByEntityEvent) deathEvent.getEntity().getLastDamageCause()).getDamager() instanceof Player &&
                deathEvent.getEntity() instanceof Monster
            ) {
                gloryData.applyGloryEvent(
                    ((EntityDamageByEntityEvent) deathEvent.getEntity().getLastDamageCause()).getDamager().getUniqueId().toString(),
                    deathEvent.getEntity().getType() + " Kill", "glory-config.events.mob-kill",
                    deathEvent.getEntity().getClass().getSimpleName().toLowerCase()
                );
                gloryDelta = true;
            }
        }

        if (gloryDelta) {
            try {
                plugin.getLogger().info(logHeader + "Attempting to save Glory Data after mob kill...");
                gloryData.save();
                plugin.getLogger().info(logHeader + "Saved Glory Data after mob kill.");
            } catch (Exception exception) {
                plugin.getLogger().severe(logHeader + "Error occurred saving Glory Data after mob kill.");
                plugin.getLogger().throwing(this.getClass().getCanonicalName(), logHeader, exception);
            }
        }
    }
}
