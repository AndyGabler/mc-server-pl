package com.gabler.huntersmc.context.guard.model;

import java.util.ArrayList;
import java.util.Arrays;

public enum GuardType {
    PATROL("Patrol", 0, "Melee guard.", "wither"),
    BRUISER("Bruiser", 1, "Heavy duty guard.", "heavy", "golem"),
    RANGER("Ranger", 2, "Ranged guard.", "skelie", "skeleton"),
    HOUND("Hound", 3, "Territory attack dog.", "ravager");

    private String simpleName;
    private int id;
    private String description;
    private ArrayList<String> aliases;

    GuardType(String simpleName, int id, String description, String... aliases) {
        this.simpleName = simpleName;
        this.id = id;
        this.description = description;
        this.aliases = new ArrayList<>();
        this.aliases.addAll(Arrays.asList(aliases));
        this.aliases.add(simpleName);
    }

    public static GuardType forAlias(String alias) {
        return Arrays.stream(GuardType.values()).filter(type ->
            type.aliases.stream().anyMatch(typeAlias -> typeAlias.equalsIgnoreCase(alias))
        ).findFirst().orElse(null);
    }

    public static GuardType forId(int id) {
        return Arrays.stream(GuardType.values()).filter(type -> type.id == id).findFirst().get();
    }

    public String getSimpleName() {
        return simpleName;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getAliases() {
        return new ArrayList<>(aliases);
    }
}
