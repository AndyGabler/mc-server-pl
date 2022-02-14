package com.gabler.huntersmc.handlers;

import com.gabler.huntersmc.context.glory.GloryData;
import com.gabler.huntersmc.context.guard.GuardData;
import com.gabler.huntersmc.context.guard.model.Guard;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.context.territory.model.Territory;
import com.gabler.huntersmc.util.TransactionUtil;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerDeathHandler implements Listener {

    private final JavaPlugin plugin;
    private final TerritoryData territoryData;
    private final GuardData guardData;
    private final GloryData gloryData;

    public PlayerDeathHandler(JavaPlugin aPlugin, TerritoryData aTerritoryData, GuardData aGuardData, GloryData aGloryData) {
        plugin = aPlugin;
        territoryData = aTerritoryData;
        guardData = aGuardData;
        gloryData = aGloryData;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        final String logHeader = TransactionUtil.rollupTransactionIdLogHeader();

        final Player deadPlayer = playerDeathEvent.getEntity();
        final Player killer = playerFromEvent(playerDeathEvent);
        if (killer == null) {
            final Guard guardKiller = guardFromEvent(playerDeathEvent);

            if (guardKiller != null) {
                plugin.getLogger().info(
                    logHeader + "A guard belong to territory " + guardKiller.getOwner().getName() + " has defeated " +
                    "player with name " + deadPlayer.getName() + "."
                );
                gloryData.applyGloryEvent(
                    guardKiller.getOwner().getOwnerUuid(), "Guard Kill", "glory-config.events.guard-defense"
                );
                gloryData.applyGloryEvent(
                    deadPlayer.getUniqueId().toString(), "Defeat", "glory-config.events.defeat-penalty"
                );
            } else {
                // PvE death
                plugin.getLogger().info(logHeader + "Determined player with name " + deadPlayer.getName() + " died to environment.");
                gloryData.applyGloryEvent(
                    deadPlayer.getUniqueId().toString(), "Environment Death", "glory-config.events.environment-death-penalty"
                );
            }
        } else {
            // PvP defeat.
            String rewardReason;
            String rewardParameter;

            final Chunk deadPlayerChunk = deadPlayer.getLocation().getChunk();
            final Chunk killerChunk = killer.getLocation().getChunk();

            final Territory deadPlayerLocale = territoryData.getTerritoryFromChunk(deadPlayerChunk.getX(), deadPlayerChunk.getZ());
            final Territory killerLocale = territoryData.getTerritoryFromChunk(killerChunk.getX(), killerChunk.getZ());

            if (killerLocale != null && killerLocale.getOwnerUuid().equalsIgnoreCase(killer.getUniqueId().toString())) {
                plugin.getLogger().info(
                    logHeader + "Player player with name " + killer.getName() + " has killed player with name " +
                    deadPlayer.getName() + " from " + killer.getName() + "'s own territory."
                );
                // Was the killer on their own territory? Minor reward.
                rewardReason = "Defense Kill";
                rewardParameter = "glory-config.events.defense-kill";
            } else if (deadPlayerLocale != null && deadPlayerLocale.getOwnerUuid().equalsIgnoreCase(deadPlayer.getUniqueId().toString())) {
                plugin.getLogger().info(
                    logHeader + "Player with name " + deadPlayer.getName() + " has been defeated by " + killer.getName() +
                    " on " + deadPlayer.getName() + "'s own territory."
                );
                // Was the victim on their own territory? Major reward.
                rewardReason = "Offense Kill";
                rewardParameter = "glory-config.events.offensive-kill";
            } else {
                plugin.getLogger().info(
                    logHeader + "Player with name " + deadPlayer.getName() + " has been defeated by " + killer.getName() +
                    " on neutral grounds."
                );
                rewardReason = "Kill";
                rewardParameter = "glory-config.events.wild-kill";
            }

            gloryData.applyGloryEvent(
                killer.getUniqueId().toString(), rewardReason, rewardParameter
            );
            gloryData.applyGloryEvent(
                deadPlayer.getUniqueId().toString(), "Defeat", "glory-config.events.defeat-penalty"
            );
        }

        try {
            plugin.getLogger().info(logHeader + "Attempting to save Glory Data after mob kill...");
            gloryData.save();
            plugin.getLogger().info(logHeader + "Saved Glory Data after mob kill.");
        } catch (Exception exception) {
            plugin.getLogger().severe(logHeader + "Error occurred saving Glory Data after mob kill.");
            plugin.getLogger().throwing(this.getClass().getCanonicalName(), logHeader, exception);
        }
    }

    private Player playerFromEvent(PlayerDeathEvent playerDeathEvent) {
        if (
            playerDeathEvent.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent &&
            ((EntityDamageByEntityEvent) playerDeathEvent.getEntity().getLastDamageCause()).getDamager() instanceof Player &&
            ((EntityDamageByEntityEvent) playerDeathEvent.getEntity().getLastDamageCause()).getDamager() != playerDeathEvent.getEntity()
        ) {
            return (Player) ((EntityDamageByEntityEvent) playerDeathEvent.getEntity().getLastDamageCause()).getDamager();
        }
        return null;
    }

    private Guard guardFromEvent(PlayerDeathEvent playerDeathEvent) {
        if (playerDeathEvent.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            return guardData.guardForUuid(
                ((EntityDamageByEntityEvent) playerDeathEvent.getEntity().getLastDamageCause()).getDamager().getUniqueId().toString()
            );
        }
        return null;
    }
}
