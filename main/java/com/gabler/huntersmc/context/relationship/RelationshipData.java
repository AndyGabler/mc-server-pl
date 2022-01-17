package com.gabler.huntersmc.context.relationship;

import com.gabler.huntersmc.context.relationship.model.PlayerRelationship;
import com.gabler.huntersmc.context.relationship.model.RelationshipType;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.util.CsvDataIntegrityException;
import com.gabler.huntersmc.util.CsvLoader;
import com.gabler.huntersmc.util.CsvRow;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RelationshipData {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private ArrayList<PlayerRelationship> relationships = new ArrayList<>();
    private final TerritoryData territoryData;
    private final CsvLoader loader;

    public RelationshipData(JavaPlugin plugin, TerritoryData aTerritoryData) throws IOException, CsvDataIntegrityException, ParseException {
        this.territoryData = aTerritoryData;
        final CsvLoader loader = new CsvLoader(plugin.getConfig().getString("relationship-data-loc"));
        loader.ensureFileExists();
        loader.load();
        loader.setMetaDataRow("player1Uuid", "territory1", "player2Uuid", "territory2", "type", "expirationDate");
        loader.save();
        this.loader = loader;

        List<CsvRow> rows = loader.getRows();
        for (final CsvRow row : rows) {
            final String player1Uuid = row.getValue("player1Uuid");
            final String territory1 = row.getValue("territory1");
            final String player2Uuid = row.getValue("player2Uuid");
            final String territory2 = row.getValue("territory2");
            final RelationshipType relationshipType = RelationshipType.forId(Integer.parseInt(row.getValue("type")));

            final String csvExpirationDate = row.getValue("expirationDate");
            Date expirationDate = null;
            if (csvExpirationDate != null) {
                final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
                expirationDate = dateFormat.parse(csvExpirationDate);
            }

            final PlayerRelationship relationship = new PlayerRelationship();
            relationship.setPlayer1Uuid(player1Uuid);
            relationship.setTerritoryName1(territory1);
            relationship.setPlayer2Uuid(player2Uuid);
            relationship.setTerritoryName2(territory2);
            relationship.setRelationshipType(relationshipType);
            relationship.setExpirationDate(expirationDate);
            relationships.add(relationship);
        }
    }

    public void save() throws IOException {
        loader.save();
    }
}
