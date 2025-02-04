package com.fabiolima.e_commerce.configuration;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *  send an order creation request to Paypal using their PaypalHttpClient.
 *  First, we need to create a PaypalHttpClient bean within our spring container
 */
@Configuration
public class PaypalConfiguration {

    @Bean
    public PayPalHttpClient getPaypalClient(
            @Value("${paypal.client-id}") String clientId,
            @Value("${paypal.secret}") String clientSecret) {
        PayPalEnvironment environment = new PayPalEnvironment.Sandbox(clientId, clientSecret);
        return new PayPalHttpClient(environment);
    }
}
