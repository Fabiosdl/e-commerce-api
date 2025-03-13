package com.fabiolima.e_commerce.service.implementation;

import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.repository.OrderRepository;
import com.fabiolima.e_commerce.service.OrderService;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PaypalServiceImpl {

    private final PayPalHttpClient payPalHttpClient;
    private final OrderService entityOrderService;
    private final OrderRepository entityOrderRepository;

    @Autowired
    public PaypalServiceImpl(PayPalHttpClient payPalHttpClient, OrderService entityOrderService, OrderRepository entityOrderRepository) {
        this.payPalHttpClient = payPalHttpClient;
        this.entityOrderService = entityOrderService;
        this.entityOrderRepository = entityOrderRepository;
    }

    public String createOrder(Long orderId) {
        //Retrieve entity order
        com.fabiolima.e_commerce.model.Order entityOrder = entityOrderService.findOrderById(orderId);

        //create the purchase unit request list
        List<PurchaseUnitRequest> purchaseUnitRequests = new ArrayList<>();

        //populate the purchase unit with entity order details
        PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
                .amountWithBreakdown(new AmountWithBreakdown()
                        .currencyCode("EUR")
                        .value(String.format("%.2f", entityOrder.getTotalPrice()))
                        .amountBreakdown(new AmountBreakdown() //in case the final price is different of the item. i.e: tax + delivery
                                .itemTotal(new Money()
                                        .currencyCode("EUR")
                                        .value(String.format("%.2f", entityOrder.getTotalPrice()))))
                )
                .items(entityOrder.getItems().stream().map(item -> new Item()
                        .name(item.getProductName())
                        .unitAmount(new Money()
                                .currencyCode("EUR")
                                .value(String.format("%.2f", item.getPrice())))
                        .quantity(String.valueOf(item.getQuantity()))
                ).toList());

        purchaseUnitRequests.add(purchaseUnit);

        //Create application context that will guide paypal after authorization
        ApplicationContext applicationContext = new ApplicationContext()
                .returnUrl("https://e-commerce-app-nine-silk.vercel.app/capture")
                .cancelUrl("https://e-commerce-app-nine-silk.vercel.app/cancel")
                .userAction("CONTINUE");

        //build PayPal order request
        OrderRequest orderRequest = new OrderRequest()
                .checkoutPaymentIntent("CAPTURE") //to capture payment
                .purchaseUnits(purchaseUnitRequests)
                .applicationContext(applicationContext);


        //call PayPal to create the order
        try {
            OrdersCreateRequest request = new OrdersCreateRequest()
                    .requestBody(orderRequest);

            HttpResponse<Order> response = payPalHttpClient.execute(request);

            log.info("PayPal Order ID: " + response.result().id());  // Log PayPal order ID
            log.info("PayPal Order Status: " + response.result().status());  // Log PayPal order status

            // Get the 'approve' URL from the PayPal response
            String approveUrl = response.result().links().stream()
                    .filter(link -> "approve".equals(link.rel()))
                    .findFirst()
                    .map(LinkDescription::href)
                    .orElseThrow(() -> new RuntimeException("No approval URL found"));

            //Bind system order with PayPal order
            String paypalOrderId = response.result().id();
            entityOrder.setPaypalOrderId(paypalOrderId);
            entityOrderRepository.save(entityOrder);

            // Redirect the user to the PayPal approval URL
            return approveUrl; // This will redirect the user to the URL approval

        } catch (Exception e) {
            log.error("Error creating PayPal order: {} ", e.getMessage());
            throw new RuntimeException("Error creating PayPal order: " + e.getMessage());
        }
    }

    public com.fabiolima.e_commerce.model.Order captureOrder(String token) {

        try {
            //create capture request
            OrdersCaptureRequest request = new OrdersCaptureRequest(token);
            request.requestBody(new OrderRequest());

            //execute capture request
            HttpResponse<Order> response = payPalHttpClient.execute(request);
            log.info("PayPal Response: Order id {} is COMPLETED", response.result().id());

            Optional<com.fabiolima.e_commerce.model.Order> systemOrder = entityOrderRepository.findByPaypalOrderId(token);
            if(systemOrder.isEmpty())
                throw new NotFoundException(String.format("Could Not Find Order containing paypal Id %s", token));

            if("COMPLETED".equalsIgnoreCase(response.result().status())){
                //retrieve system order from paypal id
                Long userId = systemOrder.get().getUser().getId();
                Long systemOrderId = systemOrder.get().getId();
                entityOrderService.updateOrderStatus(systemOrderId, "PAID");
            }

            log.info("SystemOrder status: {}", systemOrder.get().getOrderStatus());
            return systemOrder.get();

        } catch (IOException e) {
            log.error("Error capturing order: {}", e.getMessage());
            throw new RuntimeException("Error capturing order: " + e.getMessage());
        }
    }
}