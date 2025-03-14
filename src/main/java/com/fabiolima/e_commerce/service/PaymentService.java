package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.model.Payment;

import java.util.List;
import java.util.UUID;

public interface PaymentService {
    Payment processPayment(UUID orderId, String paymentMethod);
    Payment getPaymentDetails(UUID paymentId);
    List<Payment> getPaymentsByOrder(UUID orderId);
    List<Payment> getPaymentsByUser(UUID userId);

    Payment refundPayment(UUID paymentId);
    Payment confirmPayment(UUID paymentId);
    boolean isPaymentComplete(UUID orderId);
    double calculateOutstandingAmount(UUID orderId);

    boolean validatePaymentMethod(String paymentMethod);
    Payment retryFailedPayment(UUID paymentId);
}
