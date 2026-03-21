package com.springboot.projects.devvvotee.Service;

import com.springboot.projects.devvvotee.Dto.Chat.ChatResponse;
import com.springboot.projects.devvvotee.Entity.ChatEvent;
import com.springboot.projects.devvvotee.Entity.ChatMessage;
import com.springboot.projects.devvvotee.Entity.ChatSession;
import com.springboot.projects.devvvotee.enums.MessageRole;

import java.util.List;

public interface ChatService {
    List<ChatResponse> getProjectChats(Long projectId);

    ChatMessage createChatMessage(ChatSession session, List<ChatEvent>events, String content,
                                  MessageRole role, Integer tokens);
}
