package com.gabler.huntersmc.context.guard;

import com.gabler.huntersmc.context.guard.model.Guard;
import com.gabler.huntersmc.context.guard.model.GuardType;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.context.territory.model.Territory;
import com.gabler.huntersmc.context.territory.model.TerritoryChunkClaim;
import com.gabler.huntersmc.util.CsvDataIntegrityException;
import com.gabler.huntersmc.util.CsvLoader;
import com.gabler.huntersmc.util.CsvRow;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuardData {

    private final ArrayList<Guard> guards = new ArrayList<>();
    private final CsvLoader loader;
    private final JavaPlugin plugin;

    public GuardData(JavaPlugin plugin, TerritoryData territoryData) throws IOException, CsvDataIntegrityException {
        this.plugin = plugin;

        final CsvLoader loader = new CsvLoader(plugin.getConfig().getString("guard-data-loc"));
        loader.ensureFileExists();
        loader.load();
        loader.setMetaDataRow("id", "entityUuid", "guardType", "territory", "homeChunkX", "homeChunkZ");
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
            final TerritoryChunkClaim chunkClaim = territory.getClaims().stream().filter(claim ->
                claim.getX() == chunkX && claim.getZ() == chunkZ
            ).findFirst().orElse(null);
            if (chunkClaim == null) {
                throw new CsvDataIntegrityException("Guard home chunk of (" + chunkX + ", " + chunkZ + ") does not belong to territory \"" + territory.getName() + "\".");
            }

            final Guard guard = new Guard();
            guard.setCsvRowIndex(index);
            guard.setEntityUuid(entityUuid);
            guard.setHome(chunkClaim);
            guard.setId(id);
            guard.setOwner(territory);
            guard.setType(guardType);
            guards.add(guard);
        }
    }

    public void registerGuard() {
        // TODO
    }

    public void save() throws IOException {
        loader.save();
    }
}
