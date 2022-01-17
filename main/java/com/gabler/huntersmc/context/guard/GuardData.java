package com.gabler.huntersmc.context.guard;

import com.gabler.huntersmc.context.guard.model.Guard;
import com.gabler.huntersmc.context.guard.model.GuardType;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.context.territory.model.Territory;
import com.gabler.huntersmc.context.territory.model.TerritoryChunkClaim;
import com.gabler.huntersmc.util.CsvDataIntegrityException;
import com.gabler.huntersmc.util.CsvLoader;
import com.gabler.huntersmc.util.CsvRow;
import com.gabler.huntersmc.util.GuardException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GuardData {

    private final ArrayList<Guard> guards = new ArrayList<>();
    private final HashSet<String> guardUuidCache = new HashSet<>();
    private final CsvLoader loader;

    private int idCounter = 0;

    public GuardData(JavaPlugin plugin, TerritoryData territoryData) throws IOException, CsvDataIntegrityException {

        final CsvLoader loader = new CsvLoader(plugin.getConfig().getString("guard-data-loc"));
        loader.ensureFileExists();
        loader.load();
        loader.setMetaDataRow("id", "entityUuid", "guardType", "territory", "homeChunkX", "homeChunkZ", "homeX", "homeY", "homeZ");
        loader.save();
        this.loader = loader;

        List<CsvRow> rows = loader.getRows();
        for (int index = 0; index < rows.size(); index++) {
            final CsvRow row = rows.get(index);
            final int id = Integer.parseInt(row.getValue("id"));
            final String entityUuid = row.getValue("entityUuid");
            final GuardType guardType = GuardType.forId(Integer.parseInt(row.getValue("guardType")));
            final Territory territory = territoryData.getTerritoryByName(row.getValue("territory"));
            if (territory == null) {
                throw new CsvDataIntegrityException("Territory in row " + index + " does not exist.");
            }
            final int chunkX = Integer.parseInt(row.getValue("homeChunkX"));
            final int chunkZ = Integer.parseInt(row.getValue("homeChunkZ"));
            final double homeX = Double.parseDouble(row.getValue("homeX"));
            final double homeY = Double.parseDouble(row.getValue("homeY"));
            final double homeZ = Double.parseDouble(row.getValue("homeZ"));
            final TerritoryChunkClaim chunkClaim = territory.getClaims().stream().filter(claim ->
                claim.getX() == chunkX && claim.getZ() == chunkZ
            ).findFirst().orElse(null);
            if (chunkClaim == null) {
                throw new CsvDataIntegrityException("Guard home chunk of (" + chunkX + ", " + chunkZ + ") does not belong to territory \"" + territory.getName() + "\".");
            }

            idCounter = Math.max(id, idCounter);

            final Guard guard = new Guard();
            guard.setEntityUuid(entityUuid);
            guard.setHome(chunkClaim);
            guard.setId(id);
            guard.setOwner(territory);
            guard.setType(guardType);
            guard.setHomeX(homeX);
            guard.setHomeY(homeY);
            guard.setHomeZ(homeZ);
            guardUuidCache.add(entityUuid);
            guards.add(guard);
        }
    }

    public void registerGuard(
        Territory territory,
        GuardType guardType,
        String entityUuid,
        int chunkX,
        int chunkZ,
        double homeX,
        double homeY,
        double homeZ
    ) {
        idCounter++; // TODO synchronize? Maybe validate? Cap system?
        final Guard guard = new Guard();
        guard.setId(idCounter);
        guard.setType(guardType);
        guard.setOwner(territory);
        guard.setEntityUuid(entityUuid);
        guard.setHome(territory.getClaims().stream().filter(claim ->
            claim.getX() == chunkX && claim.getZ() == chunkZ
        ).findFirst().orElse(null));
        guard.setHomeX(homeX);
        guard.setHomeY(homeY);
        guard.setHomeZ(homeZ);

        if (guard.getHome() == null) {
            throw new GuardException("Chunk given for guard registration does not belong to selected territory.");
        }

        final CsvRow row = loader.newRow();

        row.setValue("id", guard.getId() + "");
        row.setValue("entityUuid", guard.getEntityUuid());
        row.setValue("guardType", guard.getType().getId() + "");
        row.setValue("territory", guard.getOwner().getName());
        row.setValue("homeChunkX", chunkX + "");
        row.setValue("homeChunkZ", chunkZ + "");
        row.setValue("homeX", homeX + "");
        row.setValue("homeY", homeY + "");
        row.setValue("homeZ", homeZ + "");

        guardUuidCache.add(entityUuid);
        guards.add(guard);
    }

    public void deleteGuard(Guard guard) {
        final CsvRow row = loader.getRowByCriteria(candidateRow -> Integer.parseInt(candidateRow.getValue("id")) == guard.getId());
        if (row != null) {
            row.delete();
        }

        guardUuidCache.remove(guard.getEntityUuid());

        // Makes sure we are able to delete copies
        guards.removeIf(globalGuard -> globalGuard.getId() == guard.getId());
    }

    public Guard guardForUuid(String uuid) {
        return guards.stream().filter(guard -> guard.getEntityUuid().equalsIgnoreCase(uuid)).findFirst().orElse(null);
    }

    public void changeGuardUuid(Guard guard, String newUuid) {
        final CsvRow guardRow = loader.getRowByCriteria(row -> Integer.parseInt(row.getValue("id"))  == guard.getId());

        if (guardRow == null) {
            throw new IllegalStateException("No CSV row found for guard with ID " + guard.getId() + ".");
        }

        guard.setEntityUuid(newUuid);
        guardRow.setValue("entityUuid", newUuid);
    }

    public List<Guard> getGuards() {
        return guards;
    }

    public boolean entityIsGuard(String uuid) {
        return guardUuidCache.contains(uuid);
    }

    public void save() throws IOException {
        loader.save();
    }
}
