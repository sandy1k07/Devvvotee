package com.springboot.projects.devvvotee.Service;

import com.springboot.projects.devvvotee.Dto.Usage.PlanLimitsResponse;
import com.springboot.projects.devvvotee.Dto.Usage.UsageTodayResponse;
import org.jspecify.annotations.Nullable;

public interface UsageService {
    UsageTodayResponse getTodayUsageOfUser(Long userId);

    PlanLimitsResponse getCurrentSubscriptionLimitsOfUser(Long userId);
}
