package com.gabler.huntersmc.handlers;

import com.gabler.huntersmc.context.guard.GuardData;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.context.territory.model.Territory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.UUID;

public class PlayerMovementHandler implements Listener {

    private final TerritoryData territoryData;
    private final GuardData guardData;
    private final HashMap<String, String> currentTerritories;

    public PlayerMovementHandler(TerritoryData aTerritoryData, GuardData aGuardData) {
        this.territoryData = aTerritoryData;
        this.guardData = aGuardData;
        currentTerritories = new HashMap<>();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        final int originalChunkX = event.getFrom().getChunk().getX();
        final int originalChunkZ = event.getFrom().getChunk().getZ();

        if (event.getTo() == null || event.getTo().getWorld().getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        final int newChunkX = event.getTo().getChunk().getX();
        final int newChunkZ = event.getTo().getChunk().getZ();
        final Territory territory = territoryData.getTerritoryFromChunk(newChunkX, newChunkZ);

        // If player is in someone else's territory, alert the guards
        if (territory != null && !territory.getOwnerUuid().equalsIgnoreCase(event.getPlayer().getUniqueId().toString())) {
            // TODO range throttle? maybe not all aggro at the same time
            guardData.getGuards().forEach(guard -> {
                if (
                    !guard.getType().isNaturallyHostile() &&
                    !guard.getOwner().getName().equalsIgnoreCase(territory.getName()) &&
                    !guard.getOwner().getOwnerUuid().equalsIgnoreCase(event.getPlayer().getUniqueId().toString())
                ) {
                    final Entity guardEntity = Bukkit.getEntity(UUID.fromString(guard.getEntityUuid()));
                    if (guardEntity != null && guardEntity instanceof Mob) {
                        ((Mob)guardEntity).setTarget(event.getPlayer());
                    }
                }
            });
        }

        // Player has moved chunks, if they've changed territories too, let them know
        if (newChunkX != originalChunkX || newChunkZ != originalChunkZ) {
            final Player player = event.getPlayer();
            final String uuid = player.getUniqueId().toString();
            final String previousChunkTerritory = currentTerritories.get(uuid);

            if (territory == null) {
                currentTerritories.put(uuid, null);
                if (previousChunkTerritory != null) {
                    player.sendTitle("Contested Territory", "Free for all.", 10, 70, 20);
                }
            } else {
                currentTerritories.put(uuid, territory.getName());
                if (!territory.getName().equalsIgnoreCase(previousChunkTerritory)) {
                    final String colorCode = TerritoryData.getColorCodeForTerritory(territory);
                    player.sendTitle(
                        ChatColor.COLOR_CHAR + "l" + colorCode + territory.getName(),
                        colorCode +"Owned by " + territory.getOwnerName(),
                        10, 70, 20
                    );
                }
            }
        }
    }
}
