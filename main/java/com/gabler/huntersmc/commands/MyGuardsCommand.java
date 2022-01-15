package com.gabler.huntersmc.commands;

import com.gabler.huntersmc.context.guard.GuardData;
import com.gabler.huntersmc.context.guard.model.Guard;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.context.territory.model.Territory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class MyGuardsCommand extends AbstractPagingCommand<Guard> {

    private static final int PAGE_LIMIT = 11;
    private final TerritoryData territoryData;
    private final GuardData guardData;

    public MyGuardsCommand(TerritoryData aTerritoryData, GuardData aGuardData) {
        this.territoryData = aTerritoryData;
        this.guardData = aGuardData;
    }

    @Override
    protected List<Guard> dataSource() {
        return guardData.getGuards();
    }

    @Override
    protected boolean filterDataWithSender(Guard guard, CommandSender sender) {
        final Territory territory = territoryData.getTerritoryByOwnerUuid(((Player)sender).getUniqueId().toString());
        return guard.getOwner().getName().equalsIgnoreCase(territory.getName());
    }

    @Override
    protected String headerName() {
        return "Your Guards";
    }

    @Override
    protected String rowFromDataSource(Guard guard) {
        String text = ChatColor.COLOR_CHAR + "a Type: " + ChatColor.COLOR_CHAR + "e" + guard.getType().getSimpleName();
        final Entity entity = Bukkit.getEntity(UUID.fromString(guard.getEntityUuid()));
        if (entity == null) {
            text += ChatColor.COLOR_CHAR + "a Status: " + ChatColor.COLOR_CHAR + "cMissing." +
                ChatColor.COLOR_CHAR + "a Location: ";
        } else {
            final Location location = entity.getLocation();
            text += ChatColor.COLOR_CHAR + "a Status: " + ChatColor.COLOR_CHAR + "eOnline." +
                ChatColor.COLOR_CHAR + "a Location: " + ChatColor.COLOR_CHAR + "e(" +
                location.getX() + ", " + location.getY() + ", " + location.getZ() + ") ";
        }

        text += ChatColor.COLOR_CHAR + "aRespawn Location: " + ChatColor.COLOR_CHAR + "e(" + guard.getHomeX() +
            ", " + guard.getHomeY() + ", " + guard.getHomeZ() + ")";
        return text;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cOnly players have guards for their territory.");
            return true;
        }

        final Territory territory = territoryData.getTerritoryByOwnerUuid(((Player)sender).getUniqueId().toString());
        if (territory == null) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cYou do not have any territory and therefore no guards.");
            return true;
        }

        final boolean hasGuards = guardData
            .getGuards()
            .stream()
            .anyMatch(guard -> guard.getOwner().getName().equalsIgnoreCase(territory.getName()));
        if (!hasGuards) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cYou do not have any guards.");
            return true;
        }

        return doOutputWithPaging(sender, args, PAGE_LIMIT);
    }
}
