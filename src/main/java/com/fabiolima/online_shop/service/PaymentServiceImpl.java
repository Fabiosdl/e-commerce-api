package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.model.Payment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Override
    public Payment processPayment(Long orderId, String paymentMethod) {

        return null;
    }

    @Override
    public Payment getPaymentDetails(Long paymentId) {
        return null;
    }

    @Override
    public List<Payment> getPaymentsByOrder(Long orderId) {
        return List.of();
    }

    @Override
    public List<Payment> getPaymentsByUser(Long userId) {
        return List.of();
    }

    @Override
    public Payment refundPayment(Long paymentId) {
        return null;
    }

    @Override
    public Payment confirmPayment(Long paymentId) {
        return null;
    }

    @Override
    public boolean isPaymentComplete(Long orderId) {
        return false;
    }

    @Override
    public double calculateOutstandingAmount(Long orderId) {
        return 0;
    }

    @Override
    public boolean validatePaymentMethod(String paymentMethod) {
        return false;
    }

    @Override
    public Payment retryFailedPayment(Long paymentId) {
        return null;
    }
}
