package com.gabler.huntersmc.context.relationship.model;

import org.bukkit.ChatColor;

import java.util.Arrays;

public enum RelationshipType {
    NEUTRAL(0, ChatColor.COLOR_CHAR + "7Nuetral"),
    WAR(1, ChatColor.COLOR_CHAR + "4War"),
    ALLY(2, ChatColor.COLOR_CHAR + "2Alliance"),
    AMBASSADOR(3, ChatColor.COLOR_CHAR + "bParley"),
    PENDING_ALLY(4, ChatColor.COLOR_CHAR + "3Pending Alliance");

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
