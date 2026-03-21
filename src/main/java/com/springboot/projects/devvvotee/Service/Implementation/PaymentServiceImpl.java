package com.springboot.projects.devvvotee.Service.Implementation;

import com.springboot.projects.devvvotee.Dto.Subscription.CheckoutRequest;
import com.springboot.projects.devvvotee.Dto.Subscription.CheckoutResponse;
import com.springboot.projects.devvvotee.Dto.Subscription.PortalResponse;
import com.springboot.projects.devvvotee.Entity.Plan;
import com.springboot.projects.devvvotee.Entity.User;
import com.springboot.projects.devvvotee.ExceptionHandling.Exception.BadRequestException;
import com.springboot.projects.devvvotee.ExceptionHandling.Exception.ResourceNotFoundException;
import com.springboot.projects.devvvotee.Repository.PlanRepository;
import com.springboot.projects.devvvotee.Repository.UserRepository;
import com.springboot.projects.devvvotee.Service.PaymentService;
import com.springboot.projects.devvvotee.Service.SubscriptionService;
import com.springboot.projects.devvvotee.Utils.HelperFunctions;
import com.springboot.projects.devvvotee.enums.SubscriptionStatus;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentServiceImpl implements PaymentService {

    final HelperFunctions functions;
    final PlanRepository planRepository;
    final UserRepository userRepository;
    final SubscriptionService subscriptionService;

    @Value("${client.url}")
    String clientUrl;

    @Override
    public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request) {
        log.info("Entered createCheckoutSessionUrl");

        Long userId = functions.getCurrentUserId();
        User user =  userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("user ", userId.toString())
        );
        Long planId = request.planId();

        Plan plan = planRepository.findById(planId).orElseThrow(
                () -> new ResourceNotFoundException("Plan", request.planId().toString())
        );

        var params = SessionCreateParams.builder()
                .addLineItem(
                        SessionCreateParams.LineItem.builder().setPrice(plan.getStripePriceId()).setQuantity(1L).build())
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(clientUrl + "/success.html?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(clientUrl + "/cancel.html")
                .putAllMetadata(Map.of("user_id", userId.toString(), "plan_id", planId.toString()));
        try {

            String stripeCustomerId = user.getStripeCustomerId();
            log.info("stripeCustomerId = {}", stripeCustomerId);
            if(stripeCustomerId == null || stripeCustomerId.isEmpty()){
                params.setCustomerEmail(user.getEmail());
            }else {
                params.setCustomer(stripeCustomerId);
            }

            Session session = Session.create(params.build());

            return new CheckoutResponse(session.getUrl());
        } catch (StripeException e) {
            log.error("Error while creating checkout session: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public PortalResponse openCustomerPortal() {
        Long userId = functions.getCurrentUserId();
        User user = functions.getCurrentUser(userId);
        String stripeCustomerId = user.getStripeCustomerId();

        if(stripeCustomerId  == null || stripeCustomerId.isEmpty()){
            throw new BadRequestException("User with id: "+ userId + ", doesn't have a stripeId");
        }

        try {
            var portalSession = com.stripe.model.billingportal.Session.create(
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setReturnUrl(clientUrl)
                            .setCustomer(stripeCustomerId)
                            .build()
            );
            return new PortalResponse(portalSession.getUrl());
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata) {
        log.info("Entered webhookEvent: " + type);
        log.info("Stripe object type: " + stripeObject.getClass().getSimpleName());

        switch(type){
            case "checkout.session.completed":
                Session session = (Session) stripeObject;
                handleCheckoutSessionCompletedEvent(session, metadata); // one time
                break;
            case "invoice.paid":
                Invoice invoice = (Invoice) stripeObject;
                handleInvoicePaidEvent(invoice);   // giving final access upon getting this event
                break;
            case "invoice.payment_failed":
                Invoice paymentFailedInvoice = (Invoice) stripeObject;
                handleInvoicePaymentFailedEvent(paymentFailedInvoice); // payment failed, marks status as PAST_DUE
                break;
            case "customer.subscription.updated":
                Subscription subscription = (Subscription) stripeObject;
                handleCustomerSubscriptionUpdatedEvent(subscription); // upon user cancellation / update
                break;
            case "customer.subscription.deleted":
                Subscription deletedSubscription = (Subscription) stripeObject;
                handleCustomerSubscriptionDeletedEvent(deletedSubscription); // subscription ends
                break;
            default: log.info("No event handler for event : " + type);
        }
    }

    private void handleCheckoutSessionCompletedEvent(Session session, Map<String, String> metadata) {
        if(session == null){
            log.error("Session object is null"); return;
        }

        Long userId = Long.valueOf(metadata.get("user_id"));
        Long planId = Long.valueOf(metadata.get("plan_id"));

        User user = functions.getCurrentUser(userId);
        String stripeCustomerId = session.getCustomer();
        String subscriptionId = session.getSubscription();

        if(user.getStripeCustomerId() == null){
            user.setStripeCustomerId(stripeCustomerId);
            userRepository.save(user);
        }
        subscriptionService.activateSubscription(userId, planId, stripeCustomerId, subscriptionId);
    }

    private void handleInvoicePaidEvent(Invoice invoice){
        String subscriptionId = extractSubscriptionFromInvoice(invoice);
        if(subscriptionId == null) return;

        try {
            Subscription subscription = Subscription.retrieve(subscriptionId); // api call to stripe server

            SubscriptionItem item = subscription.getItems().getData().get(0);
            Instant subscriptionStartTime = toInstant(item.getCurrentPeriodStart());
            Instant subscriptionEndTime = toInstant(item.getCurrentPeriodEnd());

            subscriptionService.renewSubscriptionPeriod(subscriptionId, subscriptionStartTime, subscriptionEndTime);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleCustomerSubscriptionUpdatedEvent(Subscription subscription){
        if(subscription == null){
            log.error("Subscription object is null SubscriptionUpdatedEvent"); return;
        }

        SubscriptionStatus status = mapStripeStatusToEnum(subscription.getStatus());
        if(status == null){
            log.error("unable to map SubscriptionStatus to Enum for status : " + subscription.getStatus());
            return;
        }

        SubscriptionItem item = subscription.getItems().getData().get(0);
        Instant subscriptionStartTime = toInstant(item.getCurrentPeriodStart());
        Instant subscriptionEndTime = toInstant(item.getCurrentPeriodEnd());
        Instant subscriptionCancelAt = subscription.getCancelAt() == null ? null : Instant.ofEpochSecond(subscription.getCancelAt());

        Long planId = resolvePlanId(item.getPrice());

        subscriptionService.updateSubscription(
                subscription.getId(), status, subscriptionStartTime, subscriptionEndTime, subscriptionCancelAt,
                subscription.getCancelAtPeriodEnd(), planId
        );
    }



    private void handleCustomerSubscriptionDeletedEvent(Subscription subscription){
        if(subscription == null){
            log.error("Subscription object is null for SubscriptionDeletedEvent"); return;
        }

        subscriptionService.cancelSubscription(subscription.getId());
    }

    private void handleInvoicePaymentFailedEvent(Invoice invoice){
        String subscriptionId = extractSubscriptionFromInvoice(invoice);
        if(subscriptionId == null) return;

        subscriptionService.markSubscriptionPastDue(subscriptionId);
    }

    private SubscriptionStatus mapStripeStatusToEnum(String status) {
        return switch (status) {
            case "active" -> SubscriptionStatus.ACTIVE;
            case "trialing" -> SubscriptionStatus.TRIALING;
            case "past_due", "unpaid", "paused", "incomplete_expired" -> SubscriptionStatus.PAST_DUE;
            case "canceled" -> SubscriptionStatus.CANCELED;
            case "incomplete" -> SubscriptionStatus.INCOMPLETE;
            default -> {
                log.warn("Unmapped Stripe status: {}", status);
                yield null;
            }
        };
    }

    private Instant toInstant(Long epoch) {
        return epoch != null ? Instant.ofEpochSecond(epoch) : null;
    }

    private Long resolvePlanId(Price price) {
        if (price == null || price.getId() == null) return null;
        return planRepository.findByStripePriceId(price.getId())
                .map(Plan::getId)
                .orElseThrow(() -> new ResourceNotFoundException("plan ",  price.getId()));
    }

    private String extractSubscriptionFromInvoice(Invoice invoice){
        var parent = invoice.getParent();
        if(parent == null) return null;

        var subDetails = parent.getSubscriptionDetails();
        if(subDetails == null) return null;

        return subDetails.getSubscription();
    }
}
