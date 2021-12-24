package com.gabler.huntersmc;

import com.gabler.huntersmc.commands.HuntCommand;
import com.gabler.huntersmc.context.TerritoryData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class HuntersMcPlugin extends JavaPlugin {

    private TerritoryData territoryData = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        try {
            territoryData = new TerritoryData(this);
        } catch (Exception exception) {
            getLogger().severe("Disabling HuntersMC program due to load failure." + exception);
            exception.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getCommand("hunt").setExecutor(new HuntCommand());

        getLogger().info("HuntersMC plugin has been enabled.");
    }
    @Override
    public void onDisable() {
        getLogger().info("HuntersMC plugin disabled.");
    }
}
