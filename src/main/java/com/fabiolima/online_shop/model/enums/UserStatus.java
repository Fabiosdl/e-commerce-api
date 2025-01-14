package com.fabiolima.online_shop.model.enums;

public enum UserStatus {
    ACTIVE, INACTIVE;
    // Utility method for validation
    public static boolean isValid(String status) {
        for (UserStatus userStatus : UserStatus.values()) {
            if (userStatus.name().equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    public static UserStatus fromString(String status) {
        for (UserStatus userStatus : UserStatus.values()) {
            if (userStatus.name().equalsIgnoreCase(status)) {
                return userStatus;
            }
        }
        throw new IllegalArgumentException(String.format("Invalid order status: %s", status));
    }
}