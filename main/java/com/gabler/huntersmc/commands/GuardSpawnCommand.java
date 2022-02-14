package com.gabler.huntersmc.commands;

import com.gabler.huntersmc.context.glory.GloryData;
import com.gabler.huntersmc.context.guard.GuardData;
import com.gabler.huntersmc.context.guard.model.GuardType;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.context.territory.model.Territory;
import com.gabler.huntersmc.util.GuardException;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.loot.LootTables;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GuardSpawnCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final TerritoryData territoryData;
    private final GuardData guardData;
    private final GloryData gloryData;

    public GuardSpawnCommand(JavaPlugin aPlugin, TerritoryData aTerritoryData, GuardData aGuardData, GloryData aGloryData) {
        this.plugin = aPlugin;
        this.territoryData = aTerritoryData;
        this.guardData = aGuardData;
        this.gloryData = aGloryData;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cMust be a player to execute this command.");
            return true;
        }

        if (((Player) sender).getLocation().getWorld().getEnvironment() != World.Environment.NORMAL) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cCan only guard in the overworld.");
            return true;
        }

        if (args.length != 1) {
            return false;
        }

        final Player player = (Player) sender;
        final String uuid = player.getUniqueId().toString();
        final Chunk playerChunk = player.getLocation().getChunk();
        final Territory currentTerritory = territoryData.getTerritoryFromChunk(playerChunk.getX(), playerChunk.getZ());

        if (currentTerritory == null || !currentTerritory.getOwnerUuid().equalsIgnoreCase(uuid)) {
            player.sendMessage(ChatColor.COLOR_CHAR + "cCan only place a guard in your own territory.");
            return true;
        }

        final GuardType guardType = GuardType.forAlias(args[0]);
        if (guardType == null) {
            player.sendMessage(ChatColor.COLOR_CHAR + "cNo guard with alias \"" + args[0] + "\" found. (See /guardtype command)");
            return true;
        }

        final int cost = plugin.getConfig().getInt("glory-config.price.guard-spawn." + guardType.getSimpleName().toLowerCase());
        final Integer gloryAmount = gloryData.gloryAmountForPlayer(uuid);
        if (gloryAmount == null) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cYou have no glory profile. Notify admin.");
            return true;
        } else if (gloryAmount < cost) {
            sender.sendMessage(
                ChatColor.COLOR_CHAR + "cSpawning a " + guardType.getSimpleName() + " requires " + cost + " glory. " +
                "You only have " + gloryAmount + "."
            );
            return true;
        }

        final String colorCode = TerritoryData.getColorCodeForTerritory(currentTerritory);
        final Mob guard = spawnMobForGuardType(guardType, player);
        guard.setCustomName(colorCode + currentTerritory.getName() + " " + guardType.getSimpleName());
        guard.setRemoveWhenFarAway(false);
        guard.setLootTable(LootTables.EMPTY.getLootTable());

        try {
            guardData.registerGuard(
                currentTerritory,
                guardType,
                guard.getUniqueId().toString(),
                player.getLocation().getChunk().getX(),
                player.getLocation().getChunk().getZ(),
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ()
            );
            gloryData.hardSetPlayerGlory(uuid, gloryAmount - cost);
            guardData.save();
            gloryData.save();
        } catch (GuardException exception) {
            guard.damage(9999999999.0);
            sender.sendMessage(ChatColor.COLOR_CHAR + "c" + exception.getMessage());
            return true;
        } catch (Exception exception) {
            guard.damage(9999999999.0);
            exception.printStackTrace();
            sender.sendMessage(
                ChatColor.COLOR_CHAR +
                "4Unknown error occurred. Please report timestamp to admin: " +
                new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date())
            );
            return true;
        }

        player.sendMessage(ChatColor.COLOR_CHAR + "a" + guardType.getSimpleName() + " will now protect " + currentTerritory.getName() + ".");
        return true;
    }

    private Mob spawnMobForGuardType(GuardType guardType, Player player) {
        switch (guardType) {
            case RANGER:
                return (Mob) player.getWorld().spawnEntity(player.getLocation(), EntityType.SKELETON, false);
            case PATROL:
                return (Mob) player.getWorld().spawnEntity(player.getLocation(), EntityType.WITHER_SKELETON, false);
            case BRUTE:
                return (Mob) player.getWorld().spawnEntity(player.getLocation(), EntityType.IRON_GOLEM, false);
            case HOUND:
                return (Mob) player.getWorld().spawnEntity(player.getLocation(), EntityType.RAVAGER, false);
            case BRUISER:
                return (Mob) player.getWorld().spawnEntity(player.getLocation(), EntityType.PILLAGER, false);
            default:
                player.sendMessage(ChatColor.COLOR_CHAR + "4Plugin misconfigured. No spawner for entity type.");
                // Will result in NPE but this is fine.
                return null;
        }
    }
}
