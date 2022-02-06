package com.gabler.huntersmc.handlers;

import com.gabler.huntersmc.context.glory.GloryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinHandler implements Listener {

    private final GloryData gloryData;

    public PlayerJoinHandler(GloryData aGloryData) {
        this.gloryData = aGloryData;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final String uuid = event.getPlayer().getUniqueId().toString();
        gloryData.registerProfile(uuid);
    }
}
