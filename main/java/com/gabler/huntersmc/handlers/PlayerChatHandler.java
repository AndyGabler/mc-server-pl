package com.gabler.huntersmc.handlers;

import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.context.territory.model.Territory;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatHandler implements Listener {

    private final TerritoryData territoryData;

    public PlayerChatHandler(TerritoryData aTerritoryData) {
        this.territoryData = aTerritoryData;
    }

    @EventHandler
    public void chatFormat(AsyncPlayerChatEvent chatEvent) {
        final Territory territory = territoryData.getTerritoryByOwnerUuid(chatEvent.getPlayer().getUniqueId().toString());
        final String playerName = chatEvent.getPlayer().getDisplayName();
        final String message = chatEvent.getMessage();
        final StringBuilder outMessageBuilder = new StringBuilder();

        if (territory != null) {
            final String colorCode = TerritoryData.getColorCodeForTerritory(territory);
            outMessageBuilder
                .append("[")
                .append(colorCode)
                .append(territory.getName())
                .append(ChatColor.COLOR_CHAR)
                .append("f]");
        }
        outMessageBuilder.append("<");

        if (chatEvent.getPlayer().hasPermission("huntersmc.maintainence") || chatEvent.getPlayer().isOp()) {
            outMessageBuilder
                .append(ChatColor.COLOR_CHAR)
                .append("c*")
                .append(ChatColor.COLOR_CHAR)
                .append("f");
        }

        outMessageBuilder
            .append(playerName)
            .append("> ")
            .append(message);

        chatEvent.setFormat(outMessageBuilder.toString());
    }
}
