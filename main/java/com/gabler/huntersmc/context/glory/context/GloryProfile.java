package com.gabler.huntersmc.context.glory.context;

import java.util.Date;

public class GloryProfile {
    private int id;
    private String playerUuid;
    private int gloryAmount;
    private double warCrimeModifier;
    private Date latestWarCrimeDate;

    // Non-CSV fields
    private Integer cachedAllianceCount;
    private Integer cachedWarCount;
    private Integer cachedTerritoriesOverCapacityCount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public void setPlayerUuid(String playerUuid) {
        this.playerUuid = playerUuid;
    }

    public int getGloryAmount() {
        return gloryAmount;
    }

    public void setGloryAmount(int gloryAmount) {
        this.gloryAmount = gloryAmount;
    }

    public double getWarCrimeModifier() {
        return warCrimeModifier;
    }

    public void setWarCrimeModifier(double warCrimeModifier) {
        this.warCrimeModifier = warCrimeModifier;
    }

    public Date getLatestWarCrimeDate() {
        return latestWarCrimeDate;
    }

    public void setLatestWarCrimeDate(Date latestWarCrimeDate) {
        this.latestWarCrimeDate = latestWarCrimeDate;
    }

    public Integer getCachedAllianceCount() {
        return cachedAllianceCount;
    }

    public void setCachedAllianceCount(Integer cachedAllianceCount) {
        this.cachedAllianceCount = cachedAllianceCount;
    }

    public Integer getCachedWarCount() {
        return cachedWarCount;
    }

    public void setCachedWarCount(Integer cachedWarCount) {
        this.cachedWarCount = cachedWarCount;
    }

    public Integer getCachedTerritoriesOverCapacityCount() {
        return cachedTerritoriesOverCapacityCount;
    }

    public void setCachedTerritoriesOverCapacityCount(Integer cachedTerritoriesOverCapacityCount) {
        this.cachedTerritoriesOverCapacityCount = cachedTerritoriesOverCapacityCount;
    }
}
