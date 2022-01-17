package com.gabler.huntersmc.context.territory.model;

import java.util.ArrayList;

public class Territory {
    private String name;
    private String ownerName;
    private String ownerUuid;
    private ArrayList<TerritoryChunkClaim> claims = new ArrayList<>();
    private String color;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(String ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public ArrayList<TerritoryChunkClaim> getClaims() {
        return claims;
    }

    public void setClaims(ArrayList<TerritoryChunkClaim> claims) {
        this.claims = claims;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
