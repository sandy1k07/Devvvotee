package com.springboot.projects.devvvotee.Mapper;

import com.springboot.projects.devvvotee.Dto.Subscription.SubscriptionResponse;
import com.springboot.projects.devvvotee.Entity.Plan;
import com.springboot.projects.devvvotee.Entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(target = "periodEnd", source = "subscription.subscriptionStartTime")
    SubscriptionResponse toSubscriptionResponse(Subscription subscription, Plan plan);

}
