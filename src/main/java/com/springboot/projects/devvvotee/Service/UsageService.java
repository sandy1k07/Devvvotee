package com.springboot.projects.devvvotee.Service;

import com.springboot.projects.devvvotee.Dto.Usage.PlanLimitsResponse;
import com.springboot.projects.devvvotee.Dto.Usage.UsageTodayResponse;
import com.springboot.projects.devvvotee.Entity.UsageLog;
import org.jspecify.annotations.Nullable;

public interface UsageService {
    void recordTokenUsage(Integer tokenUsage);

    Boolean checkDailyTokenUsage();

    UsageLog createNewUsageLog();
}
