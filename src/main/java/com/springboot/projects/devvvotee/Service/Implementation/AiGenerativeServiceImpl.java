package com.springboot.projects.devvvotee.Service.Implementation;

import com.springboot.projects.devvvotee.Dto.Chat.AiChatResponse;
import com.springboot.projects.devvvotee.Entity.ChatEvent;
import com.springboot.projects.devvvotee.Entity.ChatMessage;
import com.springboot.projects.devvvotee.Entity.ChatSession;
import com.springboot.projects.devvvotee.Entity.ChatSessionId;
import com.springboot.projects.devvvotee.LLM.LlmResponseParser;
import com.springboot.projects.devvvotee.LLM.Prompt;
import com.springboot.projects.devvvotee.LLM.advisor.FileTreeContextAdvisor;
import com.springboot.projects.devvvotee.LLM.tool.CodeGenerationTools;
import com.springboot.projects.devvvotee.Repository.ChatEventRepository;
import com.springboot.projects.devvvotee.Repository.ChatMessageRepository;
import com.springboot.projects.devvvotee.Repository.ChatSessionRepository;
import com.springboot.projects.devvvotee.Repository.UsageLogRepository;
import com.springboot.projects.devvvotee.Service.*;
import com.springboot.projects.devvvotee.Utils.HelperFunctions;
import com.springboot.projects.devvvotee.enums.ChatEventType;
import com.springboot.projects.devvvotee.enums.MessageRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGenerativeServiceImpl implements AiGenerativeService {

    private final HelperFunctions helperFunctions;
    private final ChatClient chatClient;
    private final ChatSessionService chatSessionService;
    private final ProjectFileService projectFileService;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UsageService usageService;
    private final FileTreeContextAdvisor fileTreeContextAdvisor;
    private final ChatService chatService;
    private final LlmResponseParser responseParser;
    private static final Pattern FILE_TAG_PATTERN = Pattern.compile(
            "<file path=\"([^\"]+)\">(.*?)</file>", Pattern.DOTALL);

    @Override
    @PreAuthorize("@Security.canEditProject(#projectId)")
    public Flux<AiChatResponse> streamResponse(String userMessage, Long projectId) {
        log.info("Request in Ai chat service for projectId: {}", projectId);
        Long userId = helperFunctions.getCurrentUserId();
        StringBuilder fullResponse = new StringBuilder();
        CodeGenerationTools codeGenerationTools = new CodeGenerationTools(projectFileService, projectId);
        chatSessionService.createChatSessionIfNotExists(projectId, userId);
//        usageService.checkDailyTokenUsage();

        Map<String, Object> advisorParams = Map.of(
                "userId", userId,
                "projectId", projectId
        );
        log.info("Request in ChatResponse for userId: {} & projectId: {}", userId, projectId);

        AtomicReference<Long> startTime = new AtomicReference<>(System.currentTimeMillis());
        AtomicReference<Long> endTime = new AtomicReference<>(0L);
        AtomicReference<Usage> usage  = new AtomicReference<>(null);

        return chatClient.prompt()
                .system(Prompt.CODE_GENERATION_SYSTEM_PROMPT)
                .user(userMessage)
                .tools(codeGenerationTools)
                .advisors(advisorSpec -> {
                    advisorSpec.params(advisorParams);
                    advisorSpec.advisors(fileTreeContextAdvisor);
                })
                .stream()
                .chatResponse()
                .doOnNext(chatResponse -> {  // these are subscribers, subscribing to diff events
                    String response = chatResponse.getResult().getOutput().getText();
                    if(response != null && !response.isEmpty() && endTime.get() == 0) {
                        endTime.set(System.currentTimeMillis());
                    }
                    fullResponse.append(response);

                    // usage tokens
                    if(chatResponse.getMetadata().getUsage() != null) {
                        usage.set(chatResponse.getMetadata().getUsage());
                    }
                })
                .doOnError(error -> log.error("Response streaming threw an error: "+error.getMessage()))
                .doOnComplete(() -> {
                    Schedulers.boundedElastic().schedule(  // as 3rd party api call involved, offloading to another thread
                            () -> {
                                ChatSession chatSession = chatSessionRepository.getReferenceById(
                                        new ChatSessionId(userId, projectId)
                                );
                                parseAndSaveFiles(fullResponse.toString(), projectId);
                                Long thoughtTime = (endTime.get() - startTime.get())/1000;
                                parseLlmResponseAndSaveChats(fullResponse.toString(), chatSession, userMessage, thoughtTime, usage.get());
                            });
                })
                .retryWhen(Retry.max(0))
                .map(chatClientResponse ->
                {
                    String response = chatClientResponse.getResult().getOutput().getText();
                    return new AiChatResponse(response != null ? response : "");
                });

    }


    private void parseLlmResponseAndSaveChats(String llmResponse, ChatSession chatSession, String userMessage, Long thoughtTime
    , Usage usage) {

        if(usage != null){
            Integer totalTokensUsed = usage.getTotalTokens();
            usageService.recordTokenUsage(totalTokensUsed);
        }

        if(chatSession == null) {
            log.error("ChatSession object is null");
            throw new RuntimeException("ChatSession is null");
        }

        ChatMessage userChatMessage = chatService.createChatMessage(chatSession, null, userMessage,
                MessageRole.USER, usage.getPromptTokens());
        chatMessageRepository.save(userChatMessage);

        ChatMessage llmChatMessage = chatService.createChatMessage(chatSession, null, null,
                MessageRole.ASSISTANT, usage.getCompletionTokens());
        List<ChatEvent> chatEvents = responseParser.parseChatEvents(llmResponse, llmChatMessage);
        chatEvents.addFirst(ChatEvent.builder()
                        .chatMessage(llmChatMessage)
                        .type(ChatEventType.THOUGHT_TIME)
                        .sequence(0)
                        .content("Thought for " + thoughtTime.toString()  + " s")
                .build());
        llmChatMessage.setChatEvents(chatEvents);
        chatMessageRepository.save(llmChatMessage);
    }

    private void parseAndSaveFiles(String fullResponse, Long projectId) {
        log.info("parsing and saving the files for projectId: {}", projectId);
        Matcher matcher = FILE_TAG_PATTERN.matcher(fullResponse);

        while (matcher.find()) {
            String filePath = matcher.group(1);
            String fileContent = matcher.group(2).trim();

            projectFileService.saveFile(projectId, filePath, fileContent);
        }
    }
}
