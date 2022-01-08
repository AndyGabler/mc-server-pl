package com.gabler.huntersmc;

import com.gabler.huntersmc.commands.GuardSpawnCommand;
import com.gabler.huntersmc.commands.GuardTypesCommand;
import com.gabler.huntersmc.commands.HuntCommand;
import com.gabler.huntersmc.commands.TerritoryClaimCommand;
import com.gabler.huntersmc.context.guard.GuardData;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.handlers.PlayerMovementHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class HuntersMcPlugin extends JavaPlugin {

    private TerritoryData territoryData = null;
    private GuardData guardData = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getLogger().info("Attempting to load HuntersMC data...");
        try {
            territoryData = new TerritoryData(this);
            guardData = new GuardData(this, territoryData);
        } catch (Exception exception) {
            getLogger().severe("Disabling HuntersMC program due to load failure." + exception);
            exception.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Territories loaded.");

        getCommand("hunt").setExecutor(new HuntCommand());
        getCommand("claim").setExecutor(new TerritoryClaimCommand(territoryData));
        getCommand("guard").setExecutor(new GuardSpawnCommand(territoryData, guardData));
        getCommand("guardtypes").setExecutor(new GuardTypesCommand());
        getServer().getPluginManager().registerEvents(new PlayerMovementHandler(territoryData), this);

        getLogger().info("HuntersMC plugin has been enabled.");
    }
    @Override
    public void onDisable() {
        getLogger().info("HuntersMC plugin disabled.");

        getLogger().info("Attempting to save HuntersMC data....");
        try {
            // TODO save in one is catastrophic to dependencies
            territoryData.save();
            guardData.save();
        } catch (Exception exception) {
            getLogger().severe("HuntersMC data failed to save!");
        }
    }
}
