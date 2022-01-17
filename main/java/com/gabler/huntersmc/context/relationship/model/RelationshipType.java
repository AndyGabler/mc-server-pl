package com.gabler.huntersmc.context.relationship.model;

import java.util.Arrays;

public enum RelationshipType {
    NEUTRAL(0),
    WAR(1),
    ALLY(2),
    AMBASSADOR(3);

    private final int id;

    RelationshipType(int id) {
        this.id = id;
    }

    public static RelationshipType forId(int id) {
        return Arrays.stream(RelationshipType.values()).filter(type -> type.getId() == id).findFirst().get();
    }

    public int getId() {
        return id;
    }
}
