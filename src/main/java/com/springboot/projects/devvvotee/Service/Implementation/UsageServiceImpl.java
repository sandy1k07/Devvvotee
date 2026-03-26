package com.springboot.projects.devvvotee.Service.Implementation;

import com.springboot.projects.devvvotee.Dto.Subscription.PlanResponse;
import com.springboot.projects.devvvotee.Dto.Subscription.SubscriptionResponse;
import com.springboot.projects.devvvotee.Entity.UsageLog;
import com.springboot.projects.devvvotee.Repository.UsageLogRepository;
import com.springboot.projects.devvvotee.Service.SubscriptionService;
import com.springboot.projects.devvvotee.Service.UsageService;
import com.springboot.projects.devvvotee.Utils.HelperFunctions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UsageServiceImpl implements UsageService {

    private final UsageLogRepository usageLogRepository;
    private final HelperFunctions helperFunctions;
    private final SubscriptionService subscriptionService;

    @Override
    public void recordTokenUsage(Integer tokenUsage) {
        Long userId = helperFunctions.getCurrentUserId();
        LocalDate today = LocalDate.now();
        UsageLog usageLog = usageLogRepository.findByUserIdAndDate(userId, today).orElseGet(
                this::createNewUsageLog
        );

        Integer currentUsedTokens = usageLog.getTokensUsed();
        tokenUsage += currentUsedTokens;
        usageLog.setTokensUsed(tokenUsage);
        usageLogRepository.save(usageLog);
    }

    @Override
    public Boolean checkDailyTokenUsage() {
        Long userId = helperFunctions.getCurrentUserId();
        SubscriptionResponse subscriptionResponse = subscriptionService.getCurrentSubscription();
        PlanResponse planResponse = subscriptionResponse.plan();
        LocalDate today = LocalDate.now();
        UsageLog usageLogToday = usageLogRepository.findByUserIdAndDate(userId, today).orElseGet(
                this::createNewUsageLog
        );

        if(planResponse.unlimitedAi()) return true;

        Integer currentUsedTokens = usageLogToday.getTokensUsed();
        Integer TokenLimitForPlan = planResponse.maxTokensPerDay();
        if(TokenLimitForPlan >= currentUsedTokens) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Token limit exceeded");
        }
        return true;
    }

    @Override
    public UsageLog createNewUsageLog() {
        Long userId = helperFunctions.getCurrentUserId();
        UsageLog usageLog = UsageLog.builder()
                .tokensUsed(0)
                .user(helperFunctions.getUser(userId))
                .date(LocalDate.now())
                .build();
        return usageLogRepository.save(usageLog);
    }
}
