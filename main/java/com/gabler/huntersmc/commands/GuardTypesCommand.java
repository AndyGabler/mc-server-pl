package com.gabler.huntersmc.commands;

import com.gabler.huntersmc.context.guard.model.GuardType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class GuardTypesCommand extends AbstractPagingCommand<GuardType> {

    private static final int PAGE_LIMIT = 7;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return doOutputWithPaging(sender, args, PAGE_LIMIT);
    }

    @Override
    protected List<GuardType> dataSource() {
        return Arrays.asList(GuardType.values());
    }

    @Override
    protected String headerName() {
        return "Guard Types";
    }

    @Override
    protected String rowFromDataSource(GuardType dataSource) {
        return ChatColor.COLOR_CHAR + "c" + dataSource.getSimpleName() + ChatColor.COLOR_CHAR + "a - " +
            ChatColor.COLOR_CHAR + "e" + dataSource.getDescription();
    }
}
