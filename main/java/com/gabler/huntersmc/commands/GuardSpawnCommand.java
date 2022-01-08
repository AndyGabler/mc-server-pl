package com.gabler.huntersmc.commands;

import com.gabler.huntersmc.context.guard.GuardData;
import com.gabler.huntersmc.context.guard.model.GuardType;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.context.territory.model.Territory;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.loot.LootTables;

public class GuardSpawnCommand implements CommandExecutor {

    private final TerritoryData territoryData;
    private final GuardData guardData;

    public GuardSpawnCommand(TerritoryData aTerritoryData, GuardData aGuardData) {
        this.territoryData = aTerritoryData;
        this.guardData = aGuardData;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cMust be a player to execute this command.");
            return true;
        }

        if (args.length != 1) {
            return false;
        }

        final Player player = (Player) sender;
        final Chunk playerChunk = player.getLocation().getChunk();
        final Territory currentTerritory = territoryData.getTerritoryFromChunk(playerChunk.getX(), playerChunk.getZ());

        if (currentTerritory == null || !currentTerritory.getOwnerUuid().equalsIgnoreCase(player.getUniqueId().toString())) {
            player.sendMessage(ChatColor.COLOR_CHAR + "cCan only place a guard in your own territory.");
            return true;
        }

        final GuardType guardType = GuardType.forAlias(args[0]);
        if (guardType == null) {
            player.sendMessage(ChatColor.COLOR_CHAR + "cNo guard with alias \"" + args[0] + "\" found. (See /guardtype command)");
            return true;
        }

        // TODO ensure some kind of cap system?

        Mob guard = spawnMobForGuardType(guardType, player);
        guard.setCustomName(ChatColor.COLOR_CHAR + "6" + currentTerritory.getName() + " Guard");
        guard.setRemoveWhenFarAway(false);
        guard.setAware(false);
        guard.setLootTable(LootTables.EMPTY.getLootTable());

        // TODO save guard to some kind of database

        /*IronGolem golem = (IronGolem) player.getWorld().spawnEntity(player.getLocation(), EntityType.IRON_GOLEM);
        golem.setCustomName(ChatColor.COLOR_CHAR + "4" + currentTerritory.getName() + " Rebel");
        golem.setAware(false);
        golem.setTarget(player);
        golem.setAware(true);*/

        player.sendMessage("Guard spawned with UUID " + guard.getUniqueId());
        return true;
    }

    private Mob spawnMobForGuardType(GuardType guardType, Player player) {
        return (Mob) player.getWorld().spawnEntity(player.getLocation(), EntityType.WITHER_SKELETON);
    }
}
