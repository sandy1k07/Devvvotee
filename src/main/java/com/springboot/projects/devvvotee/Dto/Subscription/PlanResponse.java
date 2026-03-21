package com.springboot.projects.devvvotee.Dto.Subscription;

public record PlanResponse(
        Long id,
        String name,
        String price,
        Integer maxProjects,
        Integer maxTokensPerDay,
        Boolean unlimitedAi
) {
}
