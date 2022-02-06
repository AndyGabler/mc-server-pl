package com.gabler.huntersmc.commands;

import com.google.common.collect.ImmutableMap;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.function.Function;

public class ConfigurationCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private static final Converter<Integer> INT_CONVERTER = new Converter<>(Integer::parseInt);
    private static final Converter<Double> DOUBLE_CONVERTER = new Converter<>(Double::parseDouble);
    public static final Map<String, String> PARAMETERS = new ImmutableMap.Builder<String, String>()
        .put("TerritoryLimit", "territory-limit")
        .put("GloryAllyMultiplier", "glory-config.alliance-multiplier")
        .put("GloryWarMultiplier", "glory-config.war-multiplier")
        .put("GloryTerritoryMultiplier", "glory-config.territory-hog-multiplier")
        .put("StartingGlory", "glory-config.starting-glory-amount")
        .put("GuardPricePatrol", "glory-config.price.guard-spawn.patrol")
        .put("GuardPriceBrute", "glory-config.price.guard-spawn.brute")
        .put("GuardPriceRanger", "glory-config.price.guard-spawn.ranger")
        .put("GuardPriceHound", "glory-config.price.guard-spawn.hound")
        .put("GuardPriceBruiser", "glory-config.price.guard-spawn.bruiser")
        .put("TerritoryPrice", "glory-config.price.claim-territory")
        .put("DeclareWarPriceMin", "glory-config.price.declare-war-min")
        .put("DeclareWarPriceMax", "glory-config.price.declare-war-max")
        .build();

    public static final Map<String, Converter>
        PARAMETER_SETTER_CONVERTER = new ImmutableMap.Builder<String, Converter>()
        .put("TerritoryLimit", INT_CONVERTER)
        .put("GloryAllyMultiplier", DOUBLE_CONVERTER)
        .put("GloryWarMultiplier", DOUBLE_CONVERTER)
        .put("GloryTerritoryMultiplier", DOUBLE_CONVERTER)
        .put("StartingGlory", INT_CONVERTER)
        .put("GuardPricePatrol", INT_CONVERTER)
        .put("GuardPriceBrute", INT_CONVERTER)
        .put("GuardPriceRanger", INT_CONVERTER)
        .put("GuardPriceHound", INT_CONVERTER)
        .put("GuardPriceBruiser", INT_CONVERTER)
        .put("TerritoryPrice", INT_CONVERTER)
        .put("DeclareWarPriceMin", INT_CONVERTER)
        .put("DeclareWarPriceMax", INT_CONVERTER)
        .build();

    public ConfigurationCommand(JavaPlugin aPlugin) {
        this.plugin = aPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("huntersmc.update-config") && !sender.isOp()) {
            return false;
        }

        if (args.length != 2) {
            return false;
        }

        final String requestParameter = args[0];
        final String requestValue = args[1];

        final String parameter = PARAMETERS.get(requestParameter);
        if (parameter == null) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "cParameter of " + requestParameter + " does not exist.");
            return true;
        }

        try {
            plugin.getConfig().set(parameter, PARAMETER_SETTER_CONVERTER.get(requestParameter).converter.apply(requestValue));
            plugin.saveConfig();
        } catch (Exception exception) {
            exception.printStackTrace();
            sender.sendMessage(ChatColor.COLOR_CHAR + "4Error occurred. See logs.");
            return true;
        }

        plugin.getLogger().info("Parameter " + parameter + " updated by " + sender + " to " + requestValue + ".");
        sender.sendMessage(ChatColor.COLOR_CHAR + "aParameter " + parameter + " updated.");

        return true;
    }

    private static class Converter<T> {
        Converter(Function<String, T> aConverter) {
            this.converter = aConverter;
        }
        Function<String, T> converter;
    }
}
