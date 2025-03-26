package com.fabiolima.e_commerce.entities.enums;

public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED;

    public static boolean isValidEnum(String status){
        for(PaymentStatus ps : PaymentStatus.values()){
            if(ps.name().equalsIgnoreCase(status)) return true;
        }
        return false;
    }

    public static PaymentStatus getEnumFromString(String status){
        for(PaymentStatus ps : PaymentStatus.values()){
            if(ps.name().equalsIgnoreCase(status))
                return ps;
        }
        throw new IllegalArgumentException(String.format("Invalid payment status: %s", status));
    }
}
