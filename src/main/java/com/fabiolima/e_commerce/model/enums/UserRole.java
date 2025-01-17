package com.fabiolima.e_commerce.model.enums;

public enum UserRole {
    ROLE_CUSTOMER, ROLE_ADMIN;

    // Utility method for validation
    public static boolean isValid(String role) {
        for (UserRole userRole : UserRole.values()) {
            if (userRole.name().equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    public static UserRole fromString(String role) {
        for (UserRole userRole : UserRole.values()) {
            if (userRole.name().equalsIgnoreCase(role)) {
                return userRole;
            }
        }
        throw new IllegalArgumentException(String.format("Invalid user status: %s", role));
    }
}
