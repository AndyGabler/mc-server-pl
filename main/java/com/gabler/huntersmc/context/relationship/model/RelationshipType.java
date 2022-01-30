package com.gabler.huntersmc.context.relationship.model;

import java.util.Arrays;

public enum RelationshipType {
    NEUTRAL(0, "Nuetral"),
    WAR(1, "War"),
    ALLY(2, "Alliance"),
    AMBASSADOR(3, "Parley"),
    PENDING_ALLY(4, "Pending Alliance");

    private final int id;
    private final String simpleName;

    RelationshipType(int id, String aSimpleName) {
        this.id = id;
        this.simpleName = aSimpleName;
    }

    public static RelationshipType forId(int id) {
        return Arrays.stream(RelationshipType.values()).filter(type -> type.getId() == id).findFirst().get();
    }

    public int getId() {
        return id;
    }

    public String getSimpleName() {
        return simpleName;
    }
}
