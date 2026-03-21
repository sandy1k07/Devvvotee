package com.springboot.projects.devvvotee.Service;

import com.springboot.projects.devvvotee.Entity.ChatSession;

public interface ChatSessionService {
    ChatSession createChatSessionIfNotExists(Long projectId, Long userId);
}
