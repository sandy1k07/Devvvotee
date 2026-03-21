package com.springboot.projects.devvvotee.Service.Implementation;

import com.springboot.projects.devvvotee.Dto.Chat.ChatResponse;
import com.springboot.projects.devvvotee.Entity.ChatEvent;
import com.springboot.projects.devvvotee.Entity.ChatMessage;
import com.springboot.projects.devvvotee.Entity.ChatSession;
import com.springboot.projects.devvvotee.Entity.ChatSessionId;
import com.springboot.projects.devvvotee.Mapper.ChatMessageMapper;
import com.springboot.projects.devvvotee.Repository.ChatMessageRepository;
import com.springboot.projects.devvvotee.Repository.ChatSessionRepository;
import com.springboot.projects.devvvotee.Service.ChatService;
import com.springboot.projects.devvvotee.Utils.HelperFunctions;
import com.springboot.projects.devvvotee.enums.MessageRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final HelperFunctions helperFunctions;
    private final ChatMessageMapper chatMessageMapper;

    @Override
    public List<ChatResponse> getProjectChats(Long projectId) {
        Long userId = helperFunctions.getCurrentUserId();
        ChatSession chatSession = chatSessionRepository.getReferenceById(new ChatSessionId(userId, projectId));

        return chatMessageRepository.findByChatSession(chatSession).stream()
                .map(chatMessageMapper::toChatResponse)
                .toList();

    }

    @Override
    public ChatMessage createChatMessage(ChatSession session, List<ChatEvent>events, String content,
                                         MessageRole role, Integer tokens) {
        return ChatMessage.builder()
                .chatSession(session)
                .chatEvents(events)
                .content(content)
                .role(role)
                .tokensUsed(tokens)
                .createdAt(Instant.now())
                .build();
    }


}
