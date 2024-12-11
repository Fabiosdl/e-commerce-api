package com.fabiolima.online_shop.model.enums;

public enum OrderStatus {
    PENDING,
    PAID,
    COMPLETED,
    CANCELLED;

    // Utility method for validation
    public static boolean isValid(String status) {
        for (OrderStatus orderStatus : OrderStatus.values()) {
            if (orderStatus.name().equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    public static OrderStatus fromString(String status) {
        for (OrderStatus orderStatus : OrderStatus.values()) {
            if (orderStatus.name().equalsIgnoreCase(status)) {
                return orderStatus;
            }
        }
        throw new IllegalArgumentException(String.format("Invalid order status: %s", status));
    }
}
