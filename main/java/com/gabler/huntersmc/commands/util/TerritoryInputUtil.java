package com.gabler.huntersmc.commands.util;

public class TerritoryInputUtil {

    public static String territoryNameFromArguments(String[] args) {
        String territoryName = null;
        if (args.length > 0) {
            territoryName = "";
            for (int index = 0; index < args.length; index++) {
                if (index != 0) {
                    territoryName += " ";
                }

                territoryName += args[index];
            }
        }

        return territoryName;
    }
}
