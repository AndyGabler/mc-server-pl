package com.gabler.huntersmc.handlers;

import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.util.Pair;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;

public class PlayerMovementHandler implements Listener {

    private final TerritoryData territoryData;
    private final HashMap<String, String> currentTerritories;

    public PlayerMovementHandler(TerritoryData aTerritoryData) {
        this.territoryData = aTerritoryData;
        currentTerritories = new HashMap<>();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        final int originalChunkX = event.getFrom().getChunk().getX();
        final int originalChunkZ = event.getFrom().getChunk().getZ();

        if (event.getTo() == null) {
            return;
        }

        final int newChunkX = event.getTo().getChunk().getX();
        final int newChunkZ = event.getTo().getChunk().getZ();

        if (newChunkX == originalChunkX && newChunkZ == originalChunkZ) {
            return;
        }

        final Pair<String, String> territory = territoryData.getTerritoryOwnerFromChunk(newChunkX, newChunkZ);
        final Player player = event.getPlayer();
        final String uuid = player.getUniqueId().toString();
        final String previousChunkTerritory = currentTerritories.get(uuid);

        if (territory == null) {
            currentTerritories.put(uuid, null);
            if (previousChunkTerritory != null) {
                player.sendTitle("Contested Territory", "Free for all.", 10, 70, 20);
            }
        } else {
            currentTerritories.put(uuid, territory.getFirst());
            if (!territory.getFirst().equalsIgnoreCase(previousChunkTerritory)) {
                player.sendTitle(
                    ChatColor.COLOR_CHAR + "l" +ChatColor.COLOR_CHAR + "6" + territory.getFirst(),
                    ChatColor.COLOR_CHAR + "6" +"Owned by " + territory.getSecond(),
                    10, 70, 20
                );
            }
        }

        // TODO manual targeting for normally benign guards
    }
}
