package com.gabler.huntersmc.context.territory;

import com.gabler.huntersmc.context.territory.model.Territory;
import com.gabler.huntersmc.context.territory.model.TerritoryChunkClaim;
import com.gabler.huntersmc.util.CsvDataIntegrityException;
import com.gabler.huntersmc.util.CsvLoader;
import com.gabler.huntersmc.util.CsvRow;
import com.gabler.huntersmc.util.TerritoryException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TerritoryData {

    private static final int COLOR_COUNT = 8;

    private final ArrayList<Territory> territories = new ArrayList<>();
    private final CsvLoader loader;
    private final JavaPlugin plugin;

    public TerritoryData(JavaPlugin plugin) throws IOException, CsvDataIntegrityException {
        this.plugin = plugin;

        final CsvLoader loader = new CsvLoader(plugin.getConfig().getString("territory-data-loc"));
        loader.ensureFileExists();
        loader.load();
        loader.setMetaDataRow("chunkX", "chunkZ", "territory", "ownerUuid", "color");
        loader.save();
        this.loader = loader;

        List<CsvRow> rows = loader.getRows();
        for (final CsvRow row : rows) {
            final String territoryName = row.getValue("territory");
            final String ownerUuid = row.getValue("ownerUuid");
            final int chunkX = Integer.parseInt(row.getValue("chunkX"));
            final int chunkZ = Integer.parseInt(row.getValue("chunkZ"));
            final String color = row.getValue("color");

            Territory rowTerritory = territories
                    .stream()
                    .filter(territory -> territory.getName().equalsIgnoreCase(territoryName))
                    .findFirst().orElse(null);

            if (rowTerritory == null) {
                rowTerritory = new Territory();
                rowTerritory.setName(territoryName);
                rowTerritory.setOwnerUuid(ownerUuid);
                rowTerritory.setOwnerName(Bukkit.getOfflinePlayer(UUID.fromString(ownerUuid)).getName());
                rowTerritory.setColor(color);
                territories.add(rowTerritory);
            }

            final TerritoryChunkClaim claim = new TerritoryChunkClaim();
            claim.setX(chunkX);
            claim.setZ(chunkZ);
            rowTerritory.getClaims().add(claim);
        }
    }

    public Territory getTerritoryFromChunk(int chunkX, int chunkZ) {
        return territories.stream().filter(territory ->
            territory.getClaims().stream().anyMatch(claim -> claim.getX() == chunkX && claim.getZ() == chunkZ)
        ).findFirst().orElse(null);
    }

    public Territory getTerritoryByName(String name) {
        return territories.stream().filter(territory ->
            territory.getName().equalsIgnoreCase(name)
        ).findFirst().orElse(null);
    }

    public Territory getTerritoryByOwnerUuid(String uuid) {
        return territories.stream().filter(territory ->
            territory.getOwnerUuid().equalsIgnoreCase(uuid)
        ).findFirst().orElse(null);
    }

    public static String getColorCodeForTerritory(Territory territory) {
        return ChatColor.COLOR_CHAR + colorCodeForColor(territory.getColor());
    }

    private static String colorCodeForColor(String color) {
        switch (color) {
            case "YELLOW":
                return "e";
            case "BLUE":
                return "9";
            case "LIGHT PURPLE":
                return "d";
            case "GOLD":
                return "6";
            case "AQUA":
                return "b";
            case "DARK PURPLE":
                return "5";
            case "DARK BLUE":
                return "1";
            case "GRAY":
                return "7";
            default:
                throw new IllegalArgumentException("Unknown color of \"" + color + "\".");
        }
    }

    private static String colorForIndex(int index) {
        switch (index) {
            case 0:
                return "YELLOW";
            case 1:
                return "BLUE";
            case 2:
                return "LIGHT PURPLE";
            case 3:
                return "GOLD";
            case 4:
                return "AQUA";
            case 5:
                return "DARK PURPLE";
            case 6:
                return "DARK BLUE";
            case 7:
                return "GRAY";
            default:
                throw new IllegalArgumentException("Unknown color index of " + index + ".");
        }
    }

    public void claimTerritory(int chunkX, int chunkZ, String playerUuid, String requestedTerritoryName) {
        // First, ensure this player can even claim territories in the first place
        Territory playerTerritory = territories.stream().filter(territory -> territory.getOwnerUuid().equals(playerUuid)).findFirst().orElse(null);
        int territoryLimit = plugin.getConfig().getInt("territory-limit");
        if (playerTerritory != null && playerTerritory.getClaims().size() >= territoryLimit) {
            throw new TerritoryException("A maximum of " + territoryLimit + " territories can be claimed.");
        }

        // Okay, player can claim territories, but they do have a name ready right?
        if (playerTerritory != null && requestedTerritoryName != null) {
            throw new TerritoryException("You have already named your territory. You cannot rename it.");
        } else if (playerTerritory == null && requestedTerritoryName == null) {
            throw new TerritoryException("New territory must have a name.");
        }

        // Ensure territory name is unique
        if (requestedTerritoryName != null && territories.stream().anyMatch(territory -> territory.getName().equalsIgnoreCase(requestedTerritoryName))) {
            throw new TerritoryException("A territory already exists with that name.");
        }

        if (requestedTerritoryName != null && !CsvLoader.ACCEPTABLE_CHARACTERS_REGEX.matcher(requestedTerritoryName).matches()) {
            throw new TerritoryException("Illegal characters in territory name.");
        }

        // Next, ensure the territory is up for grabs
        Territory zoneOwner = territories.stream().filter(territory ->
            territory.getClaims().stream().anyMatch(claim -> claim.getX() == chunkX && claim.getZ() == chunkZ)
        ).findFirst().orElse(null);
        if (zoneOwner != null) {
            throw new TerritoryException("This zone is already owned by " + zoneOwner.getName() + ".");
        }

        // Next, ensure that if we already have territory this chunk is adjacent
        if (
            playerTerritory != null && playerTerritory.getClaims().stream().noneMatch(claim ->
                (Math.abs(claim.getX() - chunkX) == 1 && Math.abs(claim.getZ() - chunkZ) == 0) ||
                (Math.abs(claim.getX() - chunkX) == 0 && Math.abs(claim.getZ() - chunkZ) == 1)
            )
        ) {
            throw new TerritoryException("New territory must be in an adjacent chunk to a chunk you already own.");
        }

        if (playerTerritory == null) {
            final Random random = new Random();
            final String color = colorForIndex(random.nextInt(COLOR_COUNT));
            playerTerritory = new Territory();
            playerTerritory.setName(requestedTerritoryName);
            playerTerritory.setOwnerUuid(playerUuid);
            playerTerritory.setOwnerName(Bukkit.getOfflinePlayer(UUID.fromString(playerUuid)).getName());
            playerTerritory.setColor(color);
            territories.add(playerTerritory);
        }

        final TerritoryChunkClaim claim = new TerritoryChunkClaim();
        claim.setX(chunkX);
        claim.setZ(chunkZ);
        playerTerritory.getClaims().add(claim);

        final CsvRow row = loader.newRow();
        row.setValue("chunkX", claim.getX() + "");
        row.setValue("chunkZ", claim.getZ() + "");
        row.setValue("territory", playerTerritory.getName());
        row.setValue("ownerUuid", playerTerritory.getOwnerUuid());
        row.setValue("color", playerTerritory.getColor());
    }

    public void save() throws IOException {
        loader.save();
    }
}
