package com.gabler.huntersmc.context.relationship.model;

import com.gabler.huntersmc.context.territory.model.Territory;
import java.util.Date;

public class PlayerRelationship {

    private Territory territory1;
    private Territory territory2;
    private RelationshipType relationshipType;
    private Date expirationDate;
    private Territory initiator;

    public Territory getTerritory1() {
        return territory1;
    }

    public void setTerritory1(Territory territory1) {
        this.territory1 = territory1;
    }

    public Territory getTerritory2() {
        return territory2;
    }

    public void setTerritory2(Territory territory2) {
        this.territory2 = territory2;
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

    public Territory getInitiator() {
        return initiator;
    }

    public void setInitiator(Territory initiator) {
        this.initiator = initiator;
    }
}
