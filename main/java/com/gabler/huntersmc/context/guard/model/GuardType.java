package com.gabler.huntersmc.context.guard.model;

import java.util.ArrayList;
import java.util.Arrays;

public enum GuardType {
    MELEE_GUARD("melee", 0, "Melee guard.", "wither"),
    PROTO_0("proto0", 1, "Prototype filler 0."),
    PROTO_1("proto1", 2, "Prototype filler 1."),
    PROTO_2("proto2", 3, "Prototype filler 2."),
    PROTO_3("proto3", 4, "Prototype filler 3."),
    PROTO_4("proto4", 5, "Prototype filler 4."),
    PROTO_5("proto5", 6, "Prototype filler 5."),
    PROTO_6("proto6", 7, "Prototype filler 6."),
    PROTO_7("proto7", 8, "Prototype filler 7."),
    PROTO_8("proto8", 9, "Prototype filler 8."),
    PROTO_9("proto9", 10, "Prototype filler 9.");


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
