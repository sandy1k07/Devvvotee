package com.springboot.projects.devvvotee.Service.Implementation;


import com.springboot.projects.devvvotee.Dto.Subscription.SubscriptionResponse;
import com.springboot.projects.devvvotee.Entity.Plan;
import com.springboot.projects.devvvotee.Entity.Subscription;
import com.springboot.projects.devvvotee.Entity.User;
import com.springboot.projects.devvvotee.ExceptionHandling.Exception.ResourceNotFoundException;
import com.springboot.projects.devvvotee.Mapper.SubscriptionMapper;
import com.springboot.projects.devvvotee.Repository.*;
import com.springboot.projects.devvvotee.Service.SubscriptionService;
import com.springboot.projects.devvvotee.Utils.HelperFunctions;
import com.springboot.projects.devvvotee.enums.SubscriptionStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

import static com.springboot.projects.devvvotee.enums.SubscriptionStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final HelperFunctions helperFunctions;
    private final SubscriptionMapper subscriptionMapper;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final Integer FREE_TIER_ALLOWED_PROJECTS = 10;

    @Override
    @Transactional(Transactional.TxType.NEVER)
    public SubscriptionResponse getCurrentSubscription() {
        Long userId = helperFunctions.getCurrentUserId();
        Subscription currentSubscription = subscriptionRepository.findByIdAndStatusIn(
                userId, Set.of(ACTIVE, PAST_DUE, TRIALING)
        ).orElse( new Subscription());

        Plan plan  = currentSubscription.getPlan();
        return subscriptionMapper.toSubscriptionResponse(currentSubscription, plan);
    }

    @Override
    public void activateSubscription(Long userId, Long planId, String stripeCustomerId, String subscriptionId) {
        boolean exists = subscriptionRepository.existsByStripeSubscriptionId(subscriptionId);
        if(exists) return;

        User user = getUserById(userId);
        Plan plan = getPlanById(planId);

        Subscription subscription = Subscription.builder()
                .stripeSubscriptionId(subscriptionId)
                .user(user)
                .plan(plan)
                .status(INCOMPLETE)
                .build();
        subscriptionRepository.save(subscription);
    }

    @Override
    public void updateSubscription(String subscriptionId, SubscriptionStatus status, Instant subscriptionStartTime,
                                   Instant subscriptionEndTime, Instant subscriptionCancelAt, Boolean cancelAtPeriodEnd, Long planId)
    {
        log.info("Subscription id: {}", subscriptionId);
        log.info("Subscription status: {}", status);
        log.info("Subscription start time: {}", subscriptionStartTime);
        log.info("Subscription end time: {}", subscriptionEndTime);
        log.info("Subscription cancel at period end: {}", cancelAtPeriodEnd);
        log.info("Subscription cancel at: {}", subscriptionCancelAt);
        log.info("Plan Id: {}", planId);

        Subscription subscription = findSubscriptionBySubscriptionId(subscriptionId);

        if(status != null && !subscription.getStatus().equals(status)) {
            log.info("Updating subscription status from {} to {}", subscription.getStatus(), status);
            subscription.setStatus(status);
        }
        if(subscriptionStartTime != null && !subscription.getSubscriptionStartTime().equals(subscriptionStartTime)) {
            log.info("Updating subscription start time from {} to {}", subscription.getSubscriptionStartTime(), subscriptionStartTime);
            subscription.setSubscriptionStartTime(subscriptionStartTime);
        }
        if(subscriptionEndTime != null && !subscription.getSubscriptionEndTime().equals(subscriptionEndTime)) {
            log.info("Updating subscription end time from  {} to {}", subscription.getSubscriptionEndTime(), subscriptionEndTime);
            subscription.setSubscriptionEndTime(subscriptionEndTime);
        }
        if(subscription.getCancelSubscriptionEnd() == null || !subscription.getCancelSubscriptionEnd().equals(cancelAtPeriodEnd)) {
            log.info("Updating cancelAtPeriodEnd from {} to {}", subscription.getCancelSubscriptionEnd(), cancelAtPeriodEnd);
            subscription.setCancelSubscriptionEnd(cancelAtPeriodEnd);
        }
        if(subscription.getSubscriptionEndTime().equals(subscriptionCancelAt)) {
            log.info("Updating cancelAtPeriodEnd to TRUE");
            subscription.setCancelSubscriptionEnd(true);
        }

        if(planId != null && !planId.equals(subscription.getPlan().getId())){
            Plan plan = getPlanById(planId);
            log.info("Updating plan from {} to {}", subscription.getPlan().getName(), plan.getName());
            subscription.setPlan(plan);
        }
    }

    @Override
    public void cancelSubscription(String subscriptionId) {
        Subscription subscription = findSubscriptionBySubscriptionId(subscriptionId);
        subscription.setStatus(CANCELED);
        subscriptionRepository.save(subscription);
    }

    @Override
    public void renewSubscriptionPeriod(String subscriptionId, Instant subscriptionStartTime, Instant subscriptionEndTime) {
        Subscription subscription = findSubscriptionBySubscriptionId(subscriptionId);

        Instant newSubscriptionStartTime = subscriptionStartTime == null ? subscription.getSubscriptionEndTime() : subscriptionStartTime;

        subscription.setSubscriptionStartTime(newSubscriptionStartTime);
        subscription.setSubscriptionEndTime(subscriptionEndTime);

        if(subscription.getStatus().equals(PAST_DUE) || subscription.getStatus().equals(INCOMPLETE)) {
            subscription.setStatus(ACTIVE);
        }

        subscriptionRepository.save(subscription);

    }


    @Override
    public void markSubscriptionPastDue(String subscriptionId) {
        Subscription subscription = findSubscriptionBySubscriptionId(subscriptionId);

        if(subscription.getStatus().equals(PAST_DUE)) {
            log.info("Subscription is already marked as past due for subscriptionId: " + subscriptionId); return;
        }
        subscription.setStatus(PAST_DUE);
        subscriptionRepository.save(subscription);

        // notifying user via services
    }

    @Override
    public boolean canCreateNewProject() {
        SubscriptionResponse subscriptionResponse = getCurrentSubscription();
        Long userId = helperFunctions.getCurrentUserId();
        int user_owned_projects = projectMemberRepository.getCountOfUserOwnedProjects(userId);

        if(subscriptionResponse.plan() == null) return user_owned_projects < FREE_TIER_ALLOWED_PROJECTS;

        return user_owned_projects < subscriptionResponse.plan().maxProjects();
    }


    // Helper methods

    private Plan getPlanById(Long planId) {
        return planRepository.findById(planId).orElseThrow(() -> new ResourceNotFoundException("Plan ", planId.toString()));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User ", userId.toString()));
    }

    private Subscription findSubscriptionBySubscriptionId(String subscriptionId) {
        return subscriptionRepository.findByStripeSubscriptionId(subscriptionId).orElseThrow(
                () -> new ResourceNotFoundException("Subscription for user ", subscriptionId)
        );
    }
}
