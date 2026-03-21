package com.springboot.projects.devvvotee.Repository;

import com.springboot.projects.devvvotee.Entity.ChatEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatEventRepository extends JpaRepository<ChatEvent, Long> {
}
