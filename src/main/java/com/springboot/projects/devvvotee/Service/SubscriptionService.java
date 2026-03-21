package com.springboot.projects.devvvotee.Service;

import com.springboot.projects.devvvotee.Dto.Subscription.CheckoutRequest;
import com.springboot.projects.devvvotee.Dto.Subscription.CheckoutResponse;
import com.springboot.projects.devvvotee.Dto.Subscription.PortalResponse;
import com.springboot.projects.devvvotee.Dto.Subscription.SubscriptionResponse;
import com.springboot.projects.devvvotee.enums.SubscriptionStatus;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

public interface SubscriptionService {
    SubscriptionResponse getCurrentSubscription();

    void activateSubscription(Long userId, Long planId, String stripeCustomerId, String subscriptionId);

    void updateSubscription(String subscriptionId, SubscriptionStatus status, Instant subscriptionStartTime, Instant subscriptionEndTime,
                            Instant subscriptionCancelAt, Boolean cancelAtPeriodEnd, Long planId);

    void cancelSubscription(String subscriptionId);

    void renewSubscriptionPeriod(String subscriptionId, Instant subscriptionStartTime, Instant subscriptionEndTime);

    void markSubscriptionPastDue(String subscriptionId);

    boolean canCreateNewProject();
}
