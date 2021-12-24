package com.gabler.huntersmc.context;

import com.gabler.huntersmc.util.CsvDataIntegrityException;
import com.gabler.huntersmc.util.CsvLoader;
import com.gabler.huntersmc.util.CsvRow;
import com.gabler.huntersmc.util.Pair;
import com.gabler.huntersmc.util.TerritoryException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TerritoryData {

    private ArrayList<Territory> territories = new ArrayList<>();
    private CsvLoader loader;
    private JavaPlugin plugin;

    public TerritoryData(JavaPlugin plugin) throws IOException, CsvDataIntegrityException {
        this.plugin = plugin;

        final CsvLoader loader = new CsvLoader(plugin.getConfig().getString("territory-data-loc"));
        loader.ensureFileExists();
        loader.load();
        loader.setMetaDataRow("chunkX", "chunkZ", "territory", "ownerUuid");
        loader.save();
        this.loader = loader;

        List<CsvRow> rows = loader.getRows();
        for (int index = 0; index < rows.size(); index++) {
            CsvRow row = rows.get(index);
            final String territoryName = row.getValue("territory");
            final String ownerUuid = row.getValue("ownerUuid");
            final int chunkX = Integer.parseInt(row.getValue("chunkX"));
            final int chunkZ = Integer.parseInt(row.getValue("chunkZ"));

            Territory rowTerritory = territories
                .stream()
                .filter(territory -> territory.name.equalsIgnoreCase(territoryName))
                .findFirst().orElse(null);

            if (rowTerritory == null) {
                rowTerritory = new Territory();
                rowTerritory.name = territoryName;
                rowTerritory.ownerUuid = ownerUuid;
                rowTerritory.ownerName = Bukkit.getOfflinePlayer(UUID.fromString(ownerUuid)).getName();
                territories.add(rowTerritory);
            }
            
            final ChunkClaim claim = new ChunkClaim();
            claim.csvRowIndex = index;
            claim.x = chunkX;
            claim.z = chunkZ;
            rowTerritory.claims.add(claim);
        }
    }

    public Pair<String, String> getTerritoryFromChunk(int chunkX, int chunkZ) {
        Territory zoneOwner = territories.stream().filter(territory ->
            territory.claims.stream().anyMatch(claim -> claim.x == chunkX && claim.z == chunkZ)
        ).findFirst().orElse(null);

        if (zoneOwner != null) {
            return new Pair<>(zoneOwner.name, zoneOwner.ownerName);
        }
        return null;
    }

    public void claimTerritory(int chunkX, int chunkZ, String playerUuid, String requestedTerritoryName) {
        // First, ensure this player can even claim territories in the first place
        Territory playerTerritory = territories.stream().filter(territory -> territory.ownerUuid.equals(playerUuid)).findFirst().orElse(null);
        int territoryLimit = plugin.getConfig().getInt("territory-limit");
        if (playerTerritory != null && playerTerritory.claims.size() >= territoryLimit) {
            throw new TerritoryException("A maximum of " + territoryLimit + " territories can be claimed.");
        }

        // Okay, player can claim territories, but they do have a name ready right?
        if (playerTerritory != null && requestedTerritoryName != null) {
            throw new TerritoryException("You have already named your territory. You cannot rename it.");
        } else if (playerTerritory == null && requestedTerritoryName == null) {
            throw new TerritoryException("New territory must have a name.");
        }

        // Ensure territory name is unique
        if (requestedTerritoryName != null && territories.stream().anyMatch(territory -> territory.name.equalsIgnoreCase(requestedTerritoryName))) {
            throw new TerritoryException("A territory already exists with that name.");
        }

        if (requestedTerritoryName != null && !CsvLoader.ACCEPTABLE_CHARACTERS_REGEX.matcher(requestedTerritoryName).matches()) {
            throw new TerritoryException("Illegal characters in territory name.");
        }

        // Next, ensure the territory is up for grabs
        Territory zoneOwner = territories.stream().filter(territory ->
            territory.claims.stream().anyMatch(claim -> claim.x == chunkX && claim.z == chunkZ)
        ).findFirst().orElse(null);
        if (zoneOwner != null) {
            throw new TerritoryException("This zone is already owned by " + zoneOwner.name + ".");
        }

        // Next, ensure that if we already have territory this chunk is adjacent
        if (
            playerTerritory != null && !playerTerritory.claims.stream().anyMatch(claim ->
                (Math.abs(claim.x - chunkX) == 1 && Math.abs(claim.z - chunkZ) == 0) ||
                (Math.abs(claim.x - chunkX) == 0 && Math.abs(claim.z - chunkZ) == 1)
            )
        ) {
            throw new TerritoryException("New territory must be in an adjacent chunk to a chunk you already own.");
        }

        if (playerTerritory == null) {
            playerTerritory = new Territory();
            playerTerritory.name = requestedTerritoryName;
            playerTerritory.ownerUuid = playerUuid;
            playerTerritory.ownerName = Bukkit.getOfflinePlayer(UUID.fromString(playerUuid)).getName();
            territories.add(playerTerritory);
        }

        final ChunkClaim claim = new ChunkClaim();
        claim.x = chunkX;
        claim.z = chunkZ;
        playerTerritory.claims.add(claim);

        final CsvRow row = loader.newRow();
        claim.csvRowIndex = row.getOriginalIndex();

        row.setValue("chunkX", claim.x + "");
        row.setValue("chunkZ", claim.z + "");
        row.setValue("territory", playerTerritory.name);
        row.setValue("ownerUuid", playerTerritory.ownerUuid);
    }

    public void save() throws IOException {
        loader.save();
    }

    private class Territory {
        private String name;
        private String ownerName;
        private String ownerUuid;
        private ArrayList<ChunkClaim> claims = new ArrayList<>();
    }
    private class ChunkClaim {
        private int csvRowIndex;
        private int x;
        private int z;
    }
}
