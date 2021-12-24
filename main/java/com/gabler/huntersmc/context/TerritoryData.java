package com.gabler.huntersmc.context;

import com.gabler.huntersmc.util.CsvDataIntegrityException;
import com.gabler.huntersmc.util.CsvLoader;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class TerritoryData {

    public TerritoryData(JavaPlugin plugin) throws IOException, CsvDataIntegrityException {
        final CsvLoader loader = new CsvLoader(plugin.getConfig().getString("territory-data-loc"));
        loader.load();
        loader.setMetaDataRow("chunkX", "chunkY", "territory", "ownerUuid");
        loader.save();
    }
}
