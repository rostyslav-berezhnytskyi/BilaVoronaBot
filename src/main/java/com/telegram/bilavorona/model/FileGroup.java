package com.telegram.bilavorona.model;

public enum FileGroup {
    DOCUMENTATION("Документація"),
    EXAMPLES("Приклади виконаних робіт");

    private final String displayName;

    FileGroup(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static FileGroup fromDisplayName(String displayName) {
        for (FileGroup group : FileGroup.values()) {
            if (group.displayName.equals(displayName)) {
                return group;
            }
        }
        return null; // or throw an exception if preferred
    }
}
