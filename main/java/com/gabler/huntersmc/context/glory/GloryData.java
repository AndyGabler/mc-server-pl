package com.gabler.huntersmc.context.glory;

import com.gabler.huntersmc.context.glory.context.GloryProfile;
import com.gabler.huntersmc.context.relationship.RelationshipData;
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
import java.util.HashSet;
import java.util.List;

public class GloryData {

    private static final String DATE_FORMAT = "yyyy-MM-dd HHmmss";
    private final JavaPlugin plugin;
    private final ArrayList<GloryProfile> gloryProfiles = new ArrayList<>();
    private final HashSet<String> playerUuidCache = new HashSet<>();
    private final CsvLoader loader;
    private final TerritoryData territoryData;
    private final RelationshipData relationshipData;

    private int idCounter = 0;

    public GloryData(JavaPlugin plugin, TerritoryData aTerritoryData, RelationshipData aRelationshipData) throws IOException, CsvDataIntegrityException, ParseException {
        this.plugin = plugin;
        this.territoryData = aTerritoryData;
        this.relationshipData = aRelationshipData;

        final CsvLoader loader = new CsvLoader(plugin.getConfig().getString("glory-data-loc"));
        loader.ensureFileExists();
        loader.load();
        loader.setMetaDataRow("id", "playerUuid", "gloryAmount", "warCrimeModifier", "latestWarCrimeDate");
        loader.save();
        this.loader = loader;

        final List<CsvRow> rows = loader.getRows();
        for (final CsvRow row : rows) {
            final int id = Integer.parseInt(row.getValue("id"));
            final String playerUuid = row.getValue("playerUuid");
            final int gloryAmount = Integer.parseInt(row.getValue("gloryAmount"));
            final double warCrimeMultiplier = Double.parseDouble(row.getValue("warCrimeModifier"));
            final String latestWarCrimeDateCsv = row.getValue("latestWarCrimeDate");

            Date latestWarCrimeDate = null;
            if (latestWarCrimeDateCsv != null) {
                latestWarCrimeDate = new SimpleDateFormat(DATE_FORMAT).parse(latestWarCrimeDateCsv);
            }

            idCounter = Math.max(id, idCounter);

            final GloryProfile profile = new GloryProfile();
            profile.setId(id);
            profile.setPlayerUuid(playerUuid);
            profile.setGloryAmount(gloryAmount);
            profile.setWarCrimeModifier(warCrimeMultiplier);
            profile.setLatestWarCrimeDate(latestWarCrimeDate);
            gloryProfiles.add(profile);

            applyCacheInfoToProfile(profile, playerUuid);
        }
    }

    public void registerProfile(String playerUuid) {
        // Does logging since there's otherwise no logs attached to profile registration since commands are not used
        if (!playerUuidCache.contains(playerUuid)) {
            plugin.getLogger().info("No glory profile found for player with UUID " + playerUuid + ". Generating a new one.");

            idCounter++;
            final GloryProfile newProfile = new GloryProfile();
            newProfile.setId(idCounter);
            newProfile.setGloryAmount(plugin.getConfig().getInt("glory-config.starting-glory-amount"));
            newProfile.setLatestWarCrimeDate(null); // Assume no warcrimes committed
            newProfile.setWarCrimeModifier(1); // Default to no warcrimes
            newProfile.setPlayerUuid(playerUuid);
            applyCacheInfoToProfile(newProfile, playerUuid);

            final CsvRow newRow = loader.newRow();
            newRow.setValue("id", newProfile.getId() + "");
            newRow.setValue("playerUuid", newProfile.getPlayerUuid());
            newRow.setValue("gloryAmount", newProfile.getGloryAmount() + "");
            newRow.setValue("warCrimeModifier", newProfile.getWarCrimeModifier() + "");
            newRow.setValue("latestWarCrimeDate", null);
            gloryProfiles.add(newProfile);

            plugin.getLogger().info("Generated glory profile with ID " + newProfile.getId() + " for player with UUID " + playerUuid + ".");
        }
    }

    public Integer gloryAmountForPlayer(String playerUuid) {
        final GloryProfile profile = gloryProfiles.stream().filter(gloryProfile ->
            gloryProfile.getPlayerUuid().equalsIgnoreCase(playerUuid)
        ).findFirst().orElse(null);

        if (profile == null) {
            return null;
        }

        return profile.getGloryAmount();
    }

    public void hardSetPlayerGlory(String playerUuid, int gloryAmount) {
        final GloryProfile playerProfile = gloryProfiles.stream().filter(profile ->
            profile.getPlayerUuid().equalsIgnoreCase(playerUuid)
        ).findFirst().get();

        playerProfile.setGloryAmount(gloryAmount);

        loader
            .getRowByCriteria(row -> Integer.parseInt(row.getValue("id")) == playerProfile.getId())
            .setValue("id", playerProfile.getId() + "");
    }

    private void applyCacheInfoToProfile(GloryProfile profile, String playerUuid) {
        final Territory territory = territoryData.getTerritoryByOwnerUuid(playerUuid);
        if (territory == null) {
            return;
        }
        final int territoryCount = territory.getClaims().size();
        final int territoryThreshold = plugin.getConfig().getInt("territory-limit") - 1;
        profile.setCachedTerritoriesOverCapacityCount(territoryCount > territoryThreshold ? territoryCount - territoryThreshold : 1);

        profile.setCachedAllianceCount(
            (int) relationshipData.getTerritoryRelationships(territory).stream().filter(relationship ->
                relationship.getRelationshipType() == RelationshipType.ALLY
            ).count()
        );
        profile.setCachedWarCount(
            (int) relationshipData.getTerritoryRelationships(territory).stream().filter(relationship ->
                relationship.getRelationshipType() == RelationshipType.WAR
            ).count()
        );

        playerUuidCache.add(playerUuid);
    }

    public void save() throws IOException {
        loader.save();
    }
}
