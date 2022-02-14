package com.gabler.huntersmc.commands;

import com.gabler.huntersmc.HuntersMcPlugin;
import com.gabler.huntersmc.context.guard.GuardData;
import com.gabler.huntersmc.context.guard.model.Guard;
import com.gabler.huntersmc.context.territory.TerritoryData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;

import java.util.ArrayList;
import java.util.UUID;

public class GuardRespawnCommand implements CommandExecutor {

    private final HuntersMcPlugin plugin;
    private final GuardData guardData;

    public GuardRespawnCommand(HuntersMcPlugin aPlugin, GuardData aGuardData) {
        this.plugin = aPlugin;
        this.guardData = aGuardData;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("huntersmc.respawn") && !sender.isOp()) {
            return false;
        }

        ArrayList<Integer> guardIdsToRespawn = null;
        if (args.length > 1) {
            guardIdsToRespawn = new ArrayList<>();
            for (String arg : args) {
                try {
                    guardIdsToRespawn.add(Integer.parseInt(arg));
                } catch (Exception exception) {
                    sender.sendMessage(ChatColor.COLOR_CHAR + "cCannot parse argument \"" + arg + "\" to Guard ID Number.");
                }
            }
        } else {
            return false;
        }

        plugin.getLogger().info("A manual guard respawn was started by " + sender.getName() + ". Save attempt starting...");
        sender.sendMessage(ChatColor.COLOR_CHAR + "bGuard respawn initiating...");

        final ArrayList<Integer> finalGuardIds = guardIdsToRespawn;
        guardData.getGuards().forEach(guard -> {
            if (finalGuardIds != null && !finalGuardIds.contains(guard.getId())) {
                return;
            }

            final Entity entity = Bukkit.getEntity(UUID.fromString(guard.getEntityUuid()));
            if (entity == null) {
                plugin.getLogger().info("Guard with ID " + guard.getId() + " has had it's underlying entity with UUID " + guard.getEntityUuid() + "despawn. Attempting to respawn.");
                sender.sendMessage(
                    ChatColor.COLOR_CHAR + "bGaurd with ID " +
                    ChatColor.COLOR_CHAR + "f" + guard.getId() +
                    ChatColor.COLOR_CHAR + "b will be respawned."
                );

                Mob guardMob = spawnMob(guard, sender);
                plugin.getLogger().info("Guard with ID " + guard.getEntityUuid() + " will be changed to " + guardMob.getUniqueId() + ".");
                sender.sendMessage(
                    ChatColor.COLOR_CHAR + "bGuard with ID " +
                    ChatColor.COLOR_CHAR + "f" + guard.getId() +
                    ChatColor.COLOR_CHAR + "b has been respawned with new UUID " +
                    ChatColor.COLOR_CHAR + "f" + guardMob.getUniqueId() +
                    ChatColor.COLOR_CHAR + "b."
                );
                guardData.changeGuardUuid(guard, guardMob.getUniqueId().toString());
            }
        });

        sender.sendMessage(ChatColor.COLOR_CHAR + "bGuard respawning completed. Save recommended.");
        plugin.getLogger().info("Guard respawning completed.");
        return true;
    }

    private Mob spawnMob(Guard guard, CommandSender sender) {
        final World world = Bukkit.getWorlds()
            .stream()
            .filter(candidate -> candidate.getEnvironment() == World.Environment.NORMAL)
            .findFirst().get();

        EntityType entityType;
        switch (guard.getType()) {
            case RANGER:
                entityType = EntityType.SKELETON;
                break;
            case PATROL:
                entityType = EntityType.WITHER_SKELETON;
                break;
            case BRUTE:
                entityType = EntityType.IRON_GOLEM;
                break;
            case HOUND:
                entityType = EntityType.RAVAGER;
                break;
            case BRUISER:
                entityType = EntityType.PILLAGER;
                break;
            default:
                sender.sendMessage(ChatColor.COLOR_CHAR + "4Plugin misconfigured. No spawner for entity type for guard " + guard.getId() + " with type " + guard.getType() + ".");
                // Will result in NPE but this is fine.
                return null;
        }

        final String colorCode = TerritoryData.getColorCodeForTerritory(guard.getOwner());
        final Mob guardMob = (Mob) world.spawnEntity(
            new Location(world, guard.getHomeX(), guard.getHomeY(), guard.getHomeZ()),
            entityType,
            false
        );

        if (guardMob instanceof Skeleton) {
            guardMob.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
        }

        guardMob.setCustomName(colorCode + guard.getOwner().getName() + " " + guard.getType().getSimpleName());
        guardMob.setRemoveWhenFarAway(false);
        guardMob.setLootTable(LootTables.EMPTY.getLootTable());
        return guardMob;
    }
}
