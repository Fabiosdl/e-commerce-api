package com.fabiolima.online_shop.model.enums;


public enum BasketStatus {
    ACTIVE,
    INACTIVE,
    CHECKED_OUT;


    public static boolean isValid(String status) {
        for (BasketStatus userStatus : BasketStatus.values()) {
            if (userStatus.name().equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    public static BasketStatus fromString(String status) {
        for (BasketStatus basketStatus : BasketStatus.values()) {
            if (basketStatus.name().equalsIgnoreCase(status)) {
                return basketStatus;
            }
        }
        throw new IllegalArgumentException(String.format("Invalid basket status: %s", status));
    }
}
