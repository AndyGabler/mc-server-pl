package com.gabler.huntersmc.context.guard.model;

import com.gabler.huntersmc.context.territory.model.Territory;
import com.gabler.huntersmc.context.territory.model.TerritoryChunkClaim;

public class Guard {
    private int id;
    private String entityUuid;
    private GuardType type;
    private Territory owner;
    private TerritoryChunkClaim home;
    private double homeX;
    private double homeY;
    private double homeZ;

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

    public double getHomeX() {
        return homeX;
    }

    public void setHomeX(double homeX) {
        this.homeX = homeX;
    }

    public double getHomeY() {
        return homeY;
    }

    public void setHomeY(double homeY) {
        this.homeY = homeY;
    }

    public double getHomeZ() {
        return homeZ;
    }

    public void setHomeZ(double homeZ) {
        this.homeZ = homeZ;
    }
}
