package com.springboot.projects.devvvotee.Service;

import com.springboot.projects.devvvotee.Dto.Subscription.CheckoutRequest;
import com.springboot.projects.devvvotee.Dto.Subscription.CheckoutResponse;
import com.springboot.projects.devvvotee.Dto.Subscription.PortalResponse;
import com.stripe.model.StripeObject;

import java.util.Map;

public interface PaymentService {
    CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request);

    PortalResponse openCustomerPortal();

    void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata);
}
