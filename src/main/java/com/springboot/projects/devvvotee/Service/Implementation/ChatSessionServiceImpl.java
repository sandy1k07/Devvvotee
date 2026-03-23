package com.springboot.projects.devvvotee.Service.Implementation;

import com.springboot.projects.devvvotee.Entity.ChatSession;
import com.springboot.projects.devvvotee.Entity.ChatSessionId;
import com.springboot.projects.devvvotee.ExceptionHandling.Exception.BadRequestException;
import com.springboot.projects.devvvotee.Repository.ChatSessionRepository;
import com.springboot.projects.devvvotee.Repository.UserRepository;
import com.springboot.projects.devvvotee.Service.ChatSessionService;
import com.springboot.projects.devvvotee.Utils.HelperFunctions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final HelperFunctions helperFunctions;

    @Override
    public void createChatSessionIfNotExists(Long projectId, Long userId) {
        ChatSession chatSession = ChatSession.builder()
                .chatSessionId(new ChatSessionId(userId, projectId))
                .startedAt(Instant.now())
                .user(helperFunctions.getUser(userId))
                .project(helperFunctions.getProject(projectId))
                .build();

        try {
            chatSessionRepository.save(chatSession);
        } catch (DataIntegrityViolationException e) {
            log.error("Error while saving chat session: {}", e.getLocalizedMessage());
        }
    }
}
