package com.fabiolima.e_commerce.service_implementation;

import com.fabiolima.e_commerce.exceptions.OrderStatusException;
import com.fabiolima.e_commerce.exceptions.PaymentMethodException;
import com.fabiolima.e_commerce.model.Payment;
import com.fabiolima.e_commerce.model.Order;
import com.fabiolima.e_commerce.model.enums.OrderStatus;
import com.fabiolima.e_commerce.repository.PaymentRepository;
import com.fabiolima.e_commerce.service.OrderService;
import com.fabiolima.e_commerce.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final OrderService orderService;

    @Autowired
    public PaymentServiceImpl (OrderService orderService,
                               PaymentRepository paymentRepository){
        this.orderService = orderService;
    }

    @Override
    public Payment processPayment(Long orderId, String paymentMethod) {

        // check and fetch if the order exists
        Order order = orderService.findOrderById(orderId);

        // check if the order can be paid
        if(OrderStatus.PAID.equals(order.getOrderStatus()))
            throw new OrderStatusException("Order is already paid.");

        // validate order status
        if(!OrderStatus.PENDING.equals(order.getOrderStatus()))
            throw new OrderStatusException("Order is not eligible for payment");

        // validate payment method
        if(!validatePaymentMethod(paymentMethod)){
            throw new PaymentMethodException("Illegal payment method: " + paymentMethod);
        }

        // Delegate to PayPal service if payment method is PayPal
        /*if ("PAYPAL".equalsIgnoreCase(paymentMethod)) {
            PayPalPaymentResponse paypalResponse = payPalPaymentService.processPayment(
                    theOrder.getTotalPrice(), "USD", paymentMethod
            );

            // Map PayPal response to Payment entity
            Payment payment = mapPayPalResponseToPayment(paypalResponse, theOrder);

            // Save payment in repository and update order status
            Payment savedPayment = paymentRepository.save(payment);
            orderService.updateStatusOrder(theOrder.getUser().getId(), orderId, "PAID");

            return savedPayment;
        }*/


        // Add other payment providers here in the future
        throw new UnsupportedOperationException("Payment method not supported: " + paymentMethod);

        // Process payment via payment gateway
//        boolean paymentSuccess = paymentGateway.process(theOrder.getTotalPrice(), paymentMethod);
//        if (!paymentSuccess) {
//            throw new RuntimeException("Payment processing failed.");
//        }

        //return null;
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
