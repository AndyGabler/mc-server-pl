package com.gabler.huntersmc.context.guard.model;

import com.gabler.huntersmc.context.territory.model.Territory;
import com.gabler.huntersmc.context.territory.model.TerritoryChunkClaim;

public class Guard {
    private int id;
    private String entityUuid;
    private GuardType type;
    private Territory owner;
    private TerritoryChunkClaim home;

    // Callback for quick save
    private int csvRowIndex;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEntityUuid() {
        return entityUuid;
    }

    public void setEntityUuid(String entityUuid) {
        this.entityUuid = entityUuid;
    }

    public GuardType getType() {
        return type;
    }

    public void setType(GuardType type) {
        this.type = type;
    }

    public Territory getOwner() {
        return owner;
    }

    public void setOwner(Territory owner) {
        this.owner = owner;
    }

    public TerritoryChunkClaim getHome() {
        return home;
    }

    public void setHome(TerritoryChunkClaim home) {
        this.home = home;
    }

    public int getCsvRowIndex() {
        return csvRowIndex;
    }

    public void setCsvRowIndex(int csvRowIndex) {
        this.csvRowIndex = csvRowIndex;
    }
}
