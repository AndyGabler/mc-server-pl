package com.gabler.huntersmc.context.territory;

import com.gabler.huntersmc.context.territory.model.Territory;
import com.gabler.huntersmc.context.territory.model.TerritoryChunkClaim;
import com.gabler.huntersmc.util.CsvDataIntegrityException;
import com.gabler.huntersmc.util.CsvLoader;
import com.gabler.huntersmc.util.CsvRow;
import com.gabler.huntersmc.util.TerritoryException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TerritoryData {

    private final ArrayList<Territory> territories = new ArrayList<>();
    private final CsvLoader loader;
    private final JavaPlugin plugin;

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
            final CsvRow row = rows.get(index);
            final String territoryName = row.getValue("territory");
            final String ownerUuid = row.getValue("ownerUuid");
            final int chunkX = Integer.parseInt(row.getValue("chunkX"));
            final int chunkZ = Integer.parseInt(row.getValue("chunkZ"));

            Territory rowTerritory = territories
                .stream()
                .filter(territory -> territory.getName().equalsIgnoreCase(territoryName))
                .findFirst().orElse(null);

            if (rowTerritory == null) {
                rowTerritory = new Territory();
                rowTerritory.setName(territoryName);
                rowTerritory.setOwnerUuid(ownerUuid);
                rowTerritory.setOwnerName(Bukkit.getOfflinePlayer(UUID.fromString(ownerUuid)).getName());
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
            playerTerritory != null && !playerTerritory.getClaims().stream().anyMatch(claim ->
                (Math.abs(claim.getX() - chunkX) == 1 && Math.abs(claim.getZ() - chunkZ) == 0) ||
                (Math.abs(claim.getX() - chunkX) == 0 && Math.abs(claim.getZ() - chunkZ) == 1)
            )
        ) {
            throw new TerritoryException("New territory must be in an adjacent chunk to a chunk you already own.");
        }

        if (playerTerritory == null) {
            playerTerritory = new Territory();
            playerTerritory.setName(requestedTerritoryName);
            playerTerritory.setOwnerUuid(playerUuid);
            playerTerritory.setOwnerName(Bukkit.getOfflinePlayer(UUID.fromString(playerUuid)).getName());
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
    }

    public void save() throws IOException {
        loader.save();
    }
}
