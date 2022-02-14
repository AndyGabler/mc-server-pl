package com.gabler.huntersmc.context.glory;

import com.gabler.huntersmc.context.glory.context.GloryProfile;
import com.gabler.huntersmc.context.relationship.RelationshipData;
import com.gabler.huntersmc.context.relationship.model.RelationshipType;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.context.territory.model.Territory;
import com.gabler.huntersmc.util.CsvDataIntegrityException;
import com.gabler.huntersmc.util.CsvLoader;
import com.gabler.huntersmc.util.CsvRow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

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

    public void applyGloryEvent(String playerUuid, String reason, String eventParameter) {
        applyGloryEvent(playerUuid, reason, eventParameter, null, 1);
    }

    public void applyGloryEvent(String playerUuid, String reason, String eventParameter, int multiplier) {
        applyGloryEvent(playerUuid, reason, eventParameter, null, multiplier);
    }

    public void applyGloryEvent(String playerUuid, String reason, String defaultParameter, String optionalParameter) {
        applyGloryEvent(playerUuid, reason, defaultParameter, optionalParameter, 1);
    }

    public void applyGloryEvent(String playerUuid, String reason, String defaultParameter, String optionalParameter, int multiplier) {
        int gloryAmount = plugin.getConfig().getInt(defaultParameter);
        if (plugin.getConfig().contains(defaultParameter + "-" + optionalParameter)) {
            gloryAmount = plugin.getConfig().getInt(defaultParameter + "-" + optionalParameter);
        }
        if (gloryAmount > 0) {
            gainGlory(playerUuid, gloryAmount * multiplier, reason);
        } else if (gloryAmount < 0) {
            final GloryProfile playerProfile = gloryProfiles.stream().filter(profile ->
                profile.getPlayerUuid().equalsIgnoreCase(playerUuid)
            ).findFirst().get();

            plugin.getLogger().info("Penalizing player with UUID " + playerUuid + " " + gloryAmount + " glory for " + reason + ".");
            hardSetPlayerGlory(playerUuid, playerProfile.getGloryAmount() + gloryAmount);
            final Player player = Bukkit.getPlayer(UUID.fromString(playerUuid));
            player.sendMessage(
                ChatColor.COLOR_CHAR + "cYou've lost " + ChatColor.COLOR_CHAR + "e" + Math.abs(gloryAmount) + ChatColor.COLOR_CHAR +
                "c Glory for " + ChatColor.COLOR_CHAR + "e" + reason + ChatColor.COLOR_CHAR + "a."
            );
        }
    }

    public void gainGlory(String playerUuid, int gloryAmount, String reason) {
        final GloryProfile playerProfile = gloryProfiles.stream().filter(profile ->
            profile.getPlayerUuid().equalsIgnoreCase(playerUuid)
        ).findFirst().get();

        plugin.getLogger().info("Player with UUID " + playerUuid + " has gained " + gloryAmount + " glory for hitting trigger " + reason + ".");

        final double allianceMultiplier = plugin.getConfig().getDouble("glory-config.alliance-multiplier");
        final double warMultiplier = plugin.getConfig().getDouble("glory-config.war-multiplier");
        final double territoryMultiplier = plugin.getConfig().getDouble("glory-config.territory-hog-multiplier");

        final double multiplier = 1.0 +
            allianceMultiplier * ((double) playerProfile.getCachedAllianceCount()) +
            warMultiplier * ((double) playerProfile.getCachedWarCount()) +
            territoryMultiplier * ((double) playerProfile.getCachedTerritoriesOverCapacityCount());
        
        plugin.getLogger().info("Player with UUID " + playerUuid + " has glory gain multiplier of " + multiplier + ".");

        double gloryDelta = Math.max(1.0, multiplier * (double) gloryAmount);
        playerProfile.setGloryAmount(Math.max(playerProfile.getGloryAmount() + (int) gloryDelta, 0));

        loader
            .getRowByCriteria(row -> Integer.parseInt(row.getValue("id")) == playerProfile.getId())
            .setValue("gloryAmount", playerProfile.getGloryAmount() + "");

        plugin.getLogger().info("Rewarding player with UUID " + playerUuid + " " + (int) gloryDelta + " for " + reason + ".");

        final Player player = Bukkit.getPlayer(UUID.fromString(playerUuid));
        if (player != null) {
            player.sendMessage(
                ChatColor.COLOR_CHAR + "aYou've gained " + ChatColor.COLOR_CHAR + "e" + (int) gloryDelta +
                ChatColor.COLOR_CHAR + "a Glory for " + ChatColor.COLOR_CHAR + "e" + reason + ChatColor.COLOR_CHAR +
                "a."
            );
        }
    }

    public void hardSetPlayerGlory(String playerUuid, int gloryAmount) {
        plugin.getLogger().info("Setting player with UUID " + playerUuid + " glory amount to " + gloryAmount + ".");
        final GloryProfile playerProfile = gloryProfiles.stream().filter(profile ->
            profile.getPlayerUuid().equalsIgnoreCase(playerUuid)
        ).findFirst().get();

        playerProfile.setGloryAmount(Math.max(gloryAmount, 0));

        loader
            .getRowByCriteria(row -> Integer.parseInt(row.getValue("id")) == playerProfile.getId())
            .setValue("gloryAmount", playerProfile.getGloryAmount() + "");
    }

    public void changeWarCount(String playerUuid, boolean isIncrement) {
        final GloryProfile playerProfile = gloryProfiles.stream().filter(profile ->
            profile.getPlayerUuid().equalsIgnoreCase(playerUuid)
        ).findFirst().get();

        int delta = isIncrement ? 1 : -1;
        playerProfile.setCachedWarCount(playerProfile.getCachedWarCount() + delta);
    }

    public void changeAllyCount(String playerUuid, boolean isIncrement) {
        final GloryProfile playerProfile = gloryProfiles.stream().filter(profile ->
            profile.getPlayerUuid().equalsIgnoreCase(playerUuid)
        ).findFirst().get();

        int delta = isIncrement ? 1 : -1;
        playerProfile.setCachedAllianceCount(playerProfile.getCachedAllianceCount() + delta);
    }

    public void changeTerritoryCount(String playerUuid) {
        final GloryProfile playerProfile = gloryProfiles.stream().filter(profile ->
            profile.getPlayerUuid().equalsIgnoreCase(playerUuid)
        ).findFirst().get();
        applyTerritoryCacheInfoToProfile(playerProfile, playerUuid);
    }

    private void applyCacheInfoToProfile(GloryProfile profile, String playerUuid) {
        final Territory territory = applyTerritoryCacheInfoToProfile(profile, playerUuid);
        if (territory == null) {
            return;
        }

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

    private Territory applyTerritoryCacheInfoToProfile(GloryProfile profile, String playerUuid) {
        final Territory territory = territoryData.getTerritoryByOwnerUuid(playerUuid);
        if (territory == null) {
            return null;
        }
        final int territoryCount = territory.getClaims().size();
        final int territoryThreshold = plugin.getConfig().getInt("territory-limit");
        profile.setCachedTerritoriesOverCapacityCount(territoryCount > territoryThreshold ? territoryCount - territoryThreshold : 0);
        return territory;
    }

    public void save() throws IOException {
        loader.save();
    }
}
