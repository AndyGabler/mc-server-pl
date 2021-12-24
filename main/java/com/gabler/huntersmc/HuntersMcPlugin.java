package com.gabler.huntersmc;

import com.gabler.huntersmc.commands.HuntCommand;
import com.gabler.huntersmc.commands.TerritoryClaimCommand;
import com.gabler.huntersmc.context.TerritoryData;
import com.gabler.huntersmc.handlers.PlayerMovementHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class HuntersMcPlugin extends JavaPlugin {

    private TerritoryData territoryData = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getLogger().info("Attempting to load territory data...");
        try {
            territoryData = new TerritoryData(this);
        } catch (Exception exception) {
            getLogger().severe("Disabling HuntersMC program due to load failure." + exception);
            exception.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Territories loaded.");

        getCommand("hunt").setExecutor(new HuntCommand());
        getCommand("claim").setExecutor(new TerritoryClaimCommand(territoryData));
        getServer().getPluginManager().registerEvents(new PlayerMovementHandler(territoryData), this);

        getLogger().info("HuntersMC plugin has been enabled.");
    }
    @Override
    public void onDisable() {
        getLogger().info("HuntersMC plugin disabled.");

        getLogger().info("Attempting to save territory data....");
        try {
            territoryData.save();
        } catch (Exception exception) {
            getLogger().severe("Territory data failed to save!");
        }
    }
}
