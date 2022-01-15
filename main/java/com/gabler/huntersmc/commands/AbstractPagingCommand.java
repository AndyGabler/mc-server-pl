package com.gabler.huntersmc.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractPagingCommand<T> implements CommandExecutor {

    protected boolean doOutputWithPaging(CommandSender sender, String[] args, int pageLimit) {
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
        int minimumIndex = (pageNumber - 1) * pageLimit;
        int maximumIndex = minimumIndex + pageLimit - 1;

        final List<T> dataSource = dataSource().stream().filter(data -> filterDataWithSender(data, sender)).collect(Collectors.toList());
        if (minimumIndex < 0 || minimumIndex >= dataSource.size()) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cPage number out of index.");
            return true;
        }

        final int maxPageNumber = (dataSource.size() / pageLimit) + 1;
        final StringBuilder messageBuilder = new StringBuilder(
            ChatColor.COLOR_CHAR + "a" + headerName() + " (Page " +
            ChatColor.COLOR_CHAR + "e" + pageNumber +
            ChatColor.COLOR_CHAR + "a of " +
            ChatColor.COLOR_CHAR + "e" + maxPageNumber +
            ChatColor.COLOR_CHAR +"a):" +
            ChatColor.COLOR_CHAR + "f\n"
        );

        for (int index = minimumIndex; index < maximumIndex + 1 && index < dataSource.size(); index++) {
            final T data = dataSource.get(index);
            messageBuilder.append("    " + rowFromDataSource(data));

            messageBuilder.append(ChatColor.COLOR_CHAR + "f");
            if (index != maximumIndex && index != dataSource.size() - 1) {
                messageBuilder.append("\n");
            }
        }

        sender.sendMessage(messageBuilder.toString());
        return true;
    }

    protected abstract List<T> dataSource();

    protected abstract String headerName();

    protected boolean filterDataWithSender(T data, CommandSender sender) {
        return true;
    }

    protected abstract String rowFromDataSource(T dataSource);
}
