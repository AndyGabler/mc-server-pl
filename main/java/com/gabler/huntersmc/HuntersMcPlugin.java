package com.gabler.huntersmc;

import com.gabler.huntersmc.commands.HuntCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class HuntersMcPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("HuntersMC plugin has been enabled.");
        getCommand("hunt").setExecutor(new HuntCommand());
    }
    @Override
    public void onDisable() {
        getLogger().info("HuntersMC plugin disabled.");
    }
}
