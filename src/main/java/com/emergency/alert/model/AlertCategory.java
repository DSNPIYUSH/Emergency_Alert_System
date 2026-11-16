package com.emergency.alert.model;

/**
 * Emergency alert categories supported by the system.
 */
public enum AlertCategory {
    FIRE,
    FLOOD,
    EARTHQUAKE,
    SECURITY;

    public static boolean isValid(String name) {
        if (name == null) return false;
        try {
            AlertCategory.valueOf(name.toUpperCase().trim());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
