package com.gabler.huntersmc.commands;

import com.gabler.huntersmc.context.guard.model.GuardType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GuardTypesCommand implements CommandExecutor {

    private static final int PAGE_LIMIT = 7;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int pageNumber = 1;
        if (args.length > 1) {
            return false;
        } else if (args.length == 1) {
            try {
                pageNumber = Integer.parseInt(args[0]);
            } catch (Exception exception) {
                sender.sendMessage(ChatColor.COLOR_CHAR + "c\"" + args[0] + "\" is not a page number.");
                return true;
            }
        }
        int minimumIndex = (pageNumber - 1) * PAGE_LIMIT;
        int maximumIndex = minimumIndex + PAGE_LIMIT - 1;

        final GuardType[] types = GuardType.values();
        if (minimumIndex < 0 || minimumIndex >= types.length) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cPage number out of index.");
            return true;
        }

        final int maxPageNumber = (types.length / PAGE_LIMIT) + 1;
        final StringBuilder messageBuilder = new StringBuilder(
            ChatColor.COLOR_CHAR + "aGuard Types (Page " +
            ChatColor.COLOR_CHAR + "e" + pageNumber +
            ChatColor.COLOR_CHAR + "a of " +
            ChatColor.COLOR_CHAR + "e" + maxPageNumber +
            ChatColor.COLOR_CHAR +"a):" +
            ChatColor.COLOR_CHAR + "f\n"
        );

        for (int index = minimumIndex; index < maximumIndex + 1 && index < types.length; index++) {
            final GuardType guardType = types[index];
            messageBuilder.append(
                "    " + ChatColor.COLOR_CHAR + "c" + guardType.getSimpleName() + ChatColor.COLOR_CHAR + "a - " +
                ChatColor.COLOR_CHAR + "e" + guardType.getDescription() + ChatColor.COLOR_CHAR + "f"
            );
            if (index != maximumIndex && index != types.length - 1) {
                messageBuilder.append("\n");
            }
        }

        sender.sendMessage(messageBuilder.toString());
        return true;
    }
}
