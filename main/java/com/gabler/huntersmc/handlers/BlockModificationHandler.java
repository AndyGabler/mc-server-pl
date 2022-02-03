package com.gabler.huntersmc.handlers;

import com.gabler.huntersmc.context.relationship.RelationshipData;
import com.gabler.huntersmc.context.relationship.model.RelationshipType;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.context.territory.model.Territory;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Arrays;

public class BlockModificationHandler implements Listener {

    private final TerritoryData territoryData;
    private final RelationshipData relationshipData;

    public BlockModificationHandler(TerritoryData aTerritoryData, RelationshipData aRelationshipData) {
        territoryData = aTerritoryData;
        relationshipData = aRelationshipData;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        cancelIfNotAllowed(event, event, event.getPlayer());
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        cancelIfNotAllowed(event, event, event.getPlayer());
    }
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        cancelIfNotAllowed(event, event, event.getPlayer());
    }
    @EventHandler
    public void onBlockMultiPlace(BlockMultiPlaceEvent event) {
        cancelIfNotAllowed(event, event, event.getPlayer());
    }
    @EventHandler
    public void onBlockFertilize(BlockFertilizeEvent event) {
        cancelIfNotAllowed(event, event, event.getPlayer());
    }
    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        final Chunk chunk0 = event.getBlock().getChunk();
        final Chunk chunk1 = event.getToBlock().getChunk();

        // No chunk change, no problem
        if (chunk0.getX() == chunk1.getX() && chunk0.getZ() == chunk1.getZ()) {
            return;
        }
        final Territory toTerritory = territoryData.getTerritoryFromChunk(chunk1.getX(), chunk1.getZ());
        if (toTerritory == null) {
            return;
        }

        final Territory fromTerritory = territoryData.getTerritoryFromChunk(chunk0.getX(), chunk0.getZ());

        if (fromTerritory != toTerritory) {
            event.setCancelled(true);
        }
    }

    private void cancelIfNotAllowed(BlockEvent event, Cancellable cancellable, Player source) {
        if (source.hasPermission("huntersmc.ignore-territory-block-protect") || source.isOp()) {
            return;
        }

        final Block eventBlock = event.getBlock();
        cancelRestrictedInteraction(eventBlock, cancellable, source, territoryData, relationshipData);
    }

    public static void cancelRestrictedInteraction(
        Block block, Cancellable cancellable, Player source, TerritoryData territoryData, RelationshipData relationshipData
    ) {
        final Chunk chunk = block.getChunk();
        final Territory blockTerritory = territoryData.getTerritoryFromChunk(chunk.getX(), chunk.getZ());

        // Check if block place is on a territory
        if (blockTerritory == null || blockTerritory.getOwnerUuid().equals(source.getUniqueId().toString())) {
            return;
        }

        final Territory sourceTerritory = territoryData.getTerritoryByOwnerUuid(source.getUniqueId().toString());
        if (sourceTerritory != null) {
            final RelationshipType relationshipType = relationshipData.getTerritoryRelationshipType(blockTerritory, sourceTerritory);
            if (Arrays.asList(RelationshipType.WAR, RelationshipType.ALLY).contains(relationshipType)) {
                return;
            }
        }

        cancellable.setCancelled(true);
        source.sendMessage(ChatColor.COLOR_CHAR + "cCannot do that in the territory " + blockTerritory.getName() + ".");
    }
}
