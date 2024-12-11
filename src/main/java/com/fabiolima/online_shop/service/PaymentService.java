package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.model.Payment;

import java.util.List;

public interface PaymentService {
    Payment processPayment(Long orderId, String paymentMethod);
    Payment getPaymentDetails(Long paymentId);
    List<Payment> getPaymentsByOrder(Long orderId);
    List<Payment> getPaymentsByUser(Long userId);

    Payment refundPayment(Long paymentId);
    Payment confirmPayment(Long paymentId);
    boolean isPaymentComplete(Long orderId);
    double calculateOutstandingAmount(Long orderId);

    boolean validatePaymentMethod(String paymentMethod);
    Payment retryFailedPayment(Long paymentId);
}
