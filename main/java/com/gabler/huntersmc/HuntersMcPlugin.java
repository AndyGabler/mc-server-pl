package com.gabler.huntersmc;

import com.gabler.huntersmc.commands.*;
import com.gabler.huntersmc.context.glory.GloryData;
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
import com.gabler.huntersmc.handlers.PlayerDeathHandler;
import com.gabler.huntersmc.handlers.PlayerJoinHandler;
import com.gabler.huntersmc.handlers.PlayerMovementHandler;
import com.gabler.huntersmc.scheduled.TerritoryHoldingRewardRunnable;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class HuntersMcPlugin extends JavaPlugin {

    private TerritoryData territoryData = null;
    private GuardData guardData = null;
    private RelationshipData relationshipData = null;
    private GloryData gloryData = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getLogger().info("Attempting to load HuntersMC data...");
        try {
            territoryData = new TerritoryData(this);
            guardData = new GuardData(this, territoryData);
            relationshipData = new RelationshipData(this, territoryData);
            gloryData = new GloryData(this, territoryData, relationshipData);
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
        getCommand("hmcconfigure").setExecutor(new ConfigurationCommand(this));

        // PvP Encourager Commands
        getCommand("hunt").setExecutor(new HuntCommand());

        // Territory setup commands
        getCommand("claim").setExecutor(new TerritoryClaimCommand(this, territoryData, gloryData));
        getCommand("guard").setExecutor(new GuardSpawnCommand(this, territoryData, guardData, gloryData));
        getCommand("guardtypes").setExecutor(new GuardTypesCommand(this));
        getCommand("guardlist").setExecutor(new GuardListCommand(guardData));
        getCommand("myguards").setExecutor(new MyGuardsCommand(territoryData, guardData));

        // Relationship commands
        getCommand("envoy").setExecutor(new RelationshipEstablishCommand(this, territoryData, relationshipData, gloryData, RelationshipType.AMBASSADOR));
        getCommand("ally").setExecutor(new RelationshipEstablishCommand(this, territoryData, relationshipData, gloryData, RelationshipType.ALLY));
        getCommand("declarewar").setExecutor(new RelationshipEstablishCommand(this, territoryData, relationshipData, gloryData, RelationshipType.WAR));
        getCommand("eject").setExecutor(new RelationshipBreakCommand(territoryData, relationshipData, gloryData, RelationshipType.AMBASSADOR));
        getCommand("rejectalliance").setExecutor(new RelationshipBreakCommand(territoryData, relationshipData, gloryData, RelationshipType.PENDING_ALLY));
        getCommand("breakalliance").setExecutor(new RelationshipBreakCommand(territoryData, relationshipData, gloryData, RelationshipType.ALLY));
        getCommand("surrender").setExecutor(new RelationshipBreakCommand(territoryData, relationshipData, gloryData, RelationshipType.WAR));
        getCommand("surrender").setExecutor(new RelationshipBreakCommand(territoryData, relationshipData, gloryData, RelationshipType.WAR));
        getCommand("terms").setExecutor(new RelationshipTermsCommand(territoryData, relationshipData));

        // Glory commands
        getCommand("glory").setExecutor(new GloryCheckCommand(gloryData));
        getCommand("gloryset").setExecutor(new GlorySetCommand(gloryData));

        // Event Listeners
        getServer().getPluginManager().registerEvents(new PlayerMovementHandler(territoryData, guardData, relationshipData), this);
        getServer().getPluginManager().registerEvents(new EntityTargetHandler(territoryData, guardData, relationshipData), this);
        getServer().getPluginManager().registerEvents(new EntityDeathHandler(this, guardData, gloryData), this);
        getServer().getPluginManager().registerEvents(new PlayerChatHandler(territoryData), this);
        getServer().getPluginManager().registerEvents(new EntityDamageHandler(territoryData, guardData, relationshipData), this);
        getServer().getPluginManager().registerEvents(new BlockModificationHandler(this, territoryData, relationshipData, gloryData), this);
        getServer().getPluginManager().registerEvents(new InventoryOpenHandler(territoryData, relationshipData), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinHandler(gloryData), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathHandler(this, territoryData, guardData, gloryData), this);

        // Scheduled tasks
        getServer().getScheduler().scheduleSyncRepeatingTask(
            this, new TerritoryHoldingRewardRunnable(this, territoryData, gloryData), 0L, 36000L
        );

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
        gloryData.save();
        saveConfig();
    }
}
