package com.gabler.huntersmc;

import com.gabler.huntersmc.commands.GuardListCommand;
import com.gabler.huntersmc.commands.GuardRespawnCommand;
import com.gabler.huntersmc.commands.GuardSpawnCommand;
import com.gabler.huntersmc.commands.GuardTypesCommand;
import com.gabler.huntersmc.commands.HuntCommand;
import com.gabler.huntersmc.commands.HmcSaveCommand;
import com.gabler.huntersmc.commands.MyGuardsCommand;
import com.gabler.huntersmc.commands.RelationshipBreakCommand;
import com.gabler.huntersmc.commands.RelationshipEstablishCommand;
import com.gabler.huntersmc.commands.RelationshipTermsCommand;
import com.gabler.huntersmc.commands.TerritoryClaimCommand;
import com.gabler.huntersmc.context.guard.GuardData;
import com.gabler.huntersmc.context.relationship.RelationshipData;
import com.gabler.huntersmc.context.relationship.model.RelationshipType;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.handlers.BlockModificationHandler;
import com.gabler.huntersmc.handlers.EntityDamageHandler;
import com.gabler.huntersmc.handlers.EntityDeathHandler;
import com.gabler.huntersmc.handlers.EntityTargetHandler;
import com.gabler.huntersmc.handlers.InventoryOpenHandler;
import com.gabler.huntersmc.handlers.PlayerChatHandler;
import com.gabler.huntersmc.handlers.PlayerMovementHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class HuntersMcPlugin extends JavaPlugin {

    private TerritoryData territoryData = null;
    private GuardData guardData = null;
    private RelationshipData relationshipData = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getLogger().info("Attempting to load HuntersMC data...");
        try {
            territoryData = new TerritoryData(this);
            guardData = new GuardData(this, territoryData);
            relationshipData = new RelationshipData(this, territoryData);
        } catch (Exception exception) {
            getLogger().severe("Disabling HuntersMC plugin due to load failure." + exception);
            exception.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("HuntersMC data loaded.");

        // Maintenance Commands
        getCommand("hmcsave").setExecutor(new HmcSaveCommand(this));
        getCommand("hmcguardspawn").setExecutor(new GuardRespawnCommand(this, guardData));

        // PvP Encourager Commands
        getCommand("hunt").setExecutor(new HuntCommand());

        // Territory setup commands
        getCommand("claim").setExecutor(new TerritoryClaimCommand(territoryData));
        getCommand("guard").setExecutor(new GuardSpawnCommand(territoryData, guardData));
        getCommand("guardtypes").setExecutor(new GuardTypesCommand());
        getCommand("guardlist").setExecutor(new GuardListCommand(guardData));
        getCommand("myguards").setExecutor(new MyGuardsCommand(territoryData, guardData));

        // Relationship commands
        getCommand("envoy").setExecutor(new RelationshipEstablishCommand(territoryData, relationshipData, RelationshipType.AMBASSADOR));
        getCommand("ally").setExecutor(new RelationshipEstablishCommand(territoryData, relationshipData, RelationshipType.ALLY));
        getCommand("declarewar").setExecutor(new RelationshipEstablishCommand(territoryData, relationshipData, RelationshipType.WAR));
        getCommand("eject").setExecutor(new RelationshipBreakCommand(territoryData, relationshipData, RelationshipType.AMBASSADOR));
        getCommand("rejectalliance").setExecutor(new RelationshipBreakCommand(territoryData, relationshipData, RelationshipType.PENDING_ALLY));
        getCommand("breakalliance").setExecutor(new RelationshipBreakCommand(territoryData, relationshipData, RelationshipType.ALLY));
        getCommand("surrender").setExecutor(new RelationshipBreakCommand(territoryData, relationshipData, RelationshipType.WAR));
        getCommand("surrender").setExecutor(new RelationshipBreakCommand(territoryData, relationshipData, RelationshipType.WAR));
        getCommand("terms").setExecutor(new RelationshipTermsCommand(territoryData, relationshipData));

        // Event Listeners
        getServer().getPluginManager().registerEvents(new PlayerMovementHandler(territoryData, guardData, relationshipData), this);
        getServer().getPluginManager().registerEvents(new EntityTargetHandler(territoryData, guardData, relationshipData), this);
        getServer().getPluginManager().registerEvents(new EntityDeathHandler(guardData), this);
        getServer().getPluginManager().registerEvents(new PlayerChatHandler(territoryData), this);
        getServer().getPluginManager().registerEvents(new EntityDamageHandler(territoryData, guardData, relationshipData), this);
        getServer().getPluginManager().registerEvents(new BlockModificationHandler(territoryData, relationshipData), this);
        getServer().getPluginManager().registerEvents(new InventoryOpenHandler(territoryData, relationshipData), this);

        getLogger().info("HuntersMC plugin has been enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("HuntersMC plugin disabled.");

        getLogger().info("Attempting to save HuntersMC data....");
        try {
            doSave();
        } catch (Exception exception) {
            getLogger().severe("HuntersMC data failed to save!");
            exception.printStackTrace();
        }
    }

    public void doSave() throws Exception {
        // TODO save in one is catastrophic to dependencies
        territoryData.save();
        guardData.save();
        relationshipData.save();
    }
}
