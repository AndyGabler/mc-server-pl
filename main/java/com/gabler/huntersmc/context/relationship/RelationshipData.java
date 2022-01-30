package com.gabler.huntersmc.context.relationship;

import com.gabler.huntersmc.context.relationship.model.PlayerRelationship;
import com.gabler.huntersmc.context.relationship.model.RelationshipType;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.context.territory.model.Territory;
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

    private static final String DATE_FORMAT = "yyyy-MM-dd HHmmss";
    private ArrayList<PlayerRelationship> relationships = new ArrayList<>();
    private final TerritoryData territoryData;
    private final CsvLoader loader;

    public RelationshipData(JavaPlugin plugin, TerritoryData aTerritoryData) throws IOException, CsvDataIntegrityException, ParseException {
        this.territoryData = aTerritoryData;
        final CsvLoader loader = new CsvLoader(plugin.getConfig().getString("relationship-data-loc"));
        loader.ensureFileExists();
        loader.load();
        loader.setMetaDataRow("territory1", "territory2", "type", "expirationDate", "initiator");
        loader.save();
        this.loader = loader;

        List<CsvRow> rows = loader.getRows();
        // TODO validate one relationship per territory pair
        for (final CsvRow row : rows) {
            final String territory1Name = row.getValue("territory1");
            final String territory2Name = row.getValue("territory2");
            final RelationshipType relationshipType = RelationshipType.forId(Integer.parseInt(row.getValue("type")));

            final String csvExpirationDate = row.getValue("expirationDate");
            Date expirationDate = null;
            if (csvExpirationDate != null) {
                final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
                expirationDate = dateFormat.parse(csvExpirationDate);
            }

            final PlayerRelationship relationship = new PlayerRelationship();
            final Territory territory1 = territoryData.getTerritoryByName(territory1Name);
            final Territory territory2 = territoryData.getTerritoryByName(territory2Name);

            final int initiatorFlag = Integer.parseInt(row.getValue("initiator"));

            if (territory1 == null) {
                throw new CsvDataIntegrityException("No territory by name \"" + territory1Name + "\".");
            }
            if (territory2 == null) {
                throw new CsvDataIntegrityException("No territory by name \"" + territory2Name + "\".");
            }

            Territory initiator;
            if (initiatorFlag == 0) {
                initiator = territory1;
            } else if (initiatorFlag == 1) {
                initiator = territory2;
            } else {
                throw new CsvDataIntegrityException("Initiator flag of " + initiatorFlag + "is out of range (can be 0 or 1).");
            }

            relationship.setTerritory1(territory1);
            relationship.setTerritory2(territory2);
            relationship.setRelationshipType(relationshipType);
            relationship.setExpirationDate(expirationDate);
            relationship.setInitiator(initiator);
            relationships.add(relationship);
        }
    }

    public void save() throws IOException {
        loader.save();
    }

    public PlayerRelationship getTerritoryRelationship(Territory territory1, Territory territory2) {
        return relationships.stream().filter(playerRelationship ->
            (playerRelationship.getTerritory1() == territory1 && playerRelationship.getTerritory2() == territory2) ||
            (playerRelationship.getTerritory1() == territory2 && playerRelationship.getTerritory2() == territory1)
        ).findFirst().orElse(null);
    }

    public RelationshipType getTerritoryRelationshipType(Territory territory1, Territory territory2) {
        final PlayerRelationship relationship = getTerritoryRelationship(territory1, territory2);

        if (relationship == null || (relationship.getExpirationDate() != null && relationship.getExpirationDate().before(new Date()))) {
            return RelationshipType.NEUTRAL;
        }

        return relationship.getRelationshipType();
    }

    public void setTerritoryRelationshipType(
        Territory territory1,
        Territory territory2,
        RelationshipType relationshipType,
        Date expirationDate
    ) {
        PlayerRelationship relationship = getTerritoryRelationship(territory1, territory2);
        CsvRow csvRow = null;
        if (relationship == null) {
            csvRow = loader.newRow();
            csvRow.setValue("territory1", territory1.getName());
            csvRow.setValue("territory2", territory2.getName());

            relationship = new PlayerRelationship();
            relationship.setTerritory1(territory1);
            relationship.setTerritory2(territory2);
            relationships.add(relationship);
        }
        if (csvRow == null) {
            csvRow = loader.getRowByCriteria(row -> {
                final String csvTerritory1Name = row.getValue("territory1");
                final String csvTerritory2Name = row.getValue("territory2");

                boolean matchCriteria1 = csvTerritory1Name.equalsIgnoreCase(territory1.getName()) &&
                    csvTerritory2Name.equalsIgnoreCase(territory2.getName());
                boolean matchCriteria2 = csvTerritory1Name.equalsIgnoreCase(territory2.getName()) &&
                    csvTerritory2Name.equalsIgnoreCase(territory1.getName());

                return matchCriteria1 || matchCriteria2;
            });
        }

        boolean isInitiator = csvRow.getValue("territory1").equalsIgnoreCase(territory1.getName());

        relationship.setRelationshipType(relationshipType);
        relationship.setExpirationDate(expirationDate);
        relationship.setInitiator(isInitiator ? territory1 : territory2);

        csvRow.setValue("initiator", isInitiator ? "0" : "1");
        csvRow.setValue(
            "expirationDate",
            expirationDate != null ? new SimpleDateFormat(DATE_FORMAT).format(expirationDate) : null
        );
        csvRow.setValue("type", relationshipType.getId() + "");
    }
}
