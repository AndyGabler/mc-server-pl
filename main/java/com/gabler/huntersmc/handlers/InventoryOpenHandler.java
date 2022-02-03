package com.gabler.huntersmc.handlers;

import com.gabler.huntersmc.context.relationship.RelationshipData;
import com.gabler.huntersmc.context.territory.TerritoryData;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class InventoryOpenHandler implements Listener {

    private final TerritoryData territoryData;
    private final RelationshipData relationshipData;

    public InventoryOpenHandler(TerritoryData aTerritoryData, RelationshipData aRelationshipData) {
        territoryData = aTerritoryData;
        relationshipData = aRelationshipData;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer().hasPermission("huntersmc.ignore-territory-container-protect") || event.getPlayer().isOp()) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof Container)) {
            return;
        }

        final Container container = (Container) event.getInventory().getHolder();
        BlockModificationHandler.cancelRestrictedInteraction(container.getBlock(), event, (Player) event.getPlayer(), territoryData, relationshipData);
    }
}
