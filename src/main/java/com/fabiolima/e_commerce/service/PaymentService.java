package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.model.Payment;

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
