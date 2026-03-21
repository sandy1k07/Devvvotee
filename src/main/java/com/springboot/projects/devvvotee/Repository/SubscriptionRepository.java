package com.springboot.projects.devvvotee.Repository;

import com.springboot.projects.devvvotee.Entity.Subscription;
import com.springboot.projects.devvvotee.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {


    Optional<Subscription> findByIdAndStatusIn(Long userId, Set<SubscriptionStatus> statusSet);

    boolean existsByStripeSubscriptionId(String subscriptionId);

    Optional<Subscription> findByStripeSubscriptionId(String subscriptionId);
}
