package com.gabler.huntersmc.commands;

import com.gabler.huntersmc.context.guard.GuardData;
import com.gabler.huntersmc.context.guard.model.Guard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.UUID;

public class GuardListCommand extends AbstractPagingCommand<Guard> {

    private static final int PAGE_LIMIT = 11;
    private final GuardData guardData;

    public GuardListCommand(GuardData aGuardData) {
        this.guardData = aGuardData;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("huntersmc.metrics") && !sender.isOp()) {
            return false;
        }

        return doOutputWithPaging(sender, args, PAGE_LIMIT);
    }

    @Override
    protected List<Guard> dataSource() {
        return guardData.getGuards();
    }

    @Override
    protected String headerName() {
        return "Alive Guards";
    }

    @Override
    protected String rowFromDataSource(Guard guard) {
        String text =  ChatColor.COLOR_CHAR + "aID: " + ChatColor.COLOR_CHAR + "e" + guard.getId() +
            ChatColor.COLOR_CHAR + "a Territory: " + ChatColor.COLOR_CHAR + "e" + guard.getOwner().getName();

        text += ChatColor.COLOR_CHAR + "a Type: " + ChatColor.COLOR_CHAR + "e" + guard.getType().getSimpleName();
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
}
