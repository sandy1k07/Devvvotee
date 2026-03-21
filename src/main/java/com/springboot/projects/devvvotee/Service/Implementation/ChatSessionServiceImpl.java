package com.springboot.projects.devvvotee.Service.Implementation;

import com.springboot.projects.devvvotee.Entity.ChatSession;
import com.springboot.projects.devvvotee.Entity.ChatSessionId;
import com.springboot.projects.devvvotee.ExceptionHandling.Exception.BadRequestException;
import com.springboot.projects.devvvotee.Repository.ChatSessionRepository;
import com.springboot.projects.devvvotee.Repository.UserRepository;
import com.springboot.projects.devvvotee.Service.ChatSessionService;
import com.springboot.projects.devvvotee.Utils.HelperFunctions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final HelperFunctions helperFunctions;

    @Override
    public ChatSession createChatSessionIfNotExists(Long projectId, Long userId) {
        ChatSessionId chatSessionId = new ChatSessionId(projectId, userId);
        Optional<ChatSession> optionalChatSession = chatSessionRepository.findById(chatSessionId);
        if(optionalChatSession.isPresent()) return optionalChatSession.get();

        ChatSession chatSession = ChatSession.builder()
                .chatSessionId(chatSessionId)
                .startedAt(Instant.now())
                .user(helperFunctions.getUser(userId))
                .project(helperFunctions.getProject(projectId))
                .build();
        chatSessionRepository.save(chatSession);
        return chatSession;
    }
}
