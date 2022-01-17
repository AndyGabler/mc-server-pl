package com.gabler.huntersmc.context.relationship.model;

import java.util.Date;

public class PlayerRelationship {

    private String player1Uuid;
    private String territoryName1;
    private String player2Uuid;
    private String territoryName2;
    private RelationshipType relationshipType;
    private Date expirationDate;

    public String getPlayer1Uuid() {
        return player1Uuid;
    }

    public void setPlayer1Uuid(String player1Uuid) {
        this.player1Uuid = player1Uuid;
    }

    public String getTerritoryName1() {
        return territoryName1;
    }

    public void setTerritoryName1(String territoryName1) {
        this.territoryName1 = territoryName1;
    }

    public String getPlayer2Uuid() {
        return player2Uuid;
    }

    public void setPlayer2Uuid(String player2Uuid) {
        this.player2Uuid = player2Uuid;
    }

    public String getTerritoryName2() {
        return territoryName2;
    }

    public void setTerritoryName2(String territoryName2) {
        this.territoryName2 = territoryName2;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
}
