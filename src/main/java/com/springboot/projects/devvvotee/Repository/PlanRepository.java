package com.springboot.projects.devvvotee.Repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.springboot.projects.devvvotee.Entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByStripePriceId(String id);
}
