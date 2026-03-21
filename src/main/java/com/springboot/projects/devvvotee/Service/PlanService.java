package com.springboot.projects.devvvotee.Service;

import com.springboot.projects.devvvotee.Dto.Subscription.PlanResponse;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface PlanService {
    List<PlanResponse> getAllPlans();
}
