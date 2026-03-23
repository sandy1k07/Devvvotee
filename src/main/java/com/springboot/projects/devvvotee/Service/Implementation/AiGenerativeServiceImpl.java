package com.springboot.projects.devvvotee.Service.Implementation;

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
import com.springboot.projects.devvvotee.Service.AiGenerativeService;
import com.springboot.projects.devvvotee.Service.ChatService;
import com.springboot.projects.devvvotee.Service.ChatSessionService;
import com.springboot.projects.devvvotee.Service.ProjectFileService;
import com.springboot.projects.devvvotee.Utils.HelperFunctions;
import com.springboot.projects.devvvotee.enums.ChatEventType;
import com.springboot.projects.devvvotee.enums.MessageRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
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
    private final ChatEventRepository chatEventRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final FileTreeContextAdvisor fileTreeContextAdvisor;
    private final ChatService chatService;
    private final LlmResponseParser responseParser;
    private static final Pattern FILE_TAG_PATTERN = Pattern.compile(
            "<file path=\"([^\"]+)\">(.*?)</file>", Pattern.DOTALL);

    @Override
    @PreAuthorize("@Security.canEditProject(#projectId)")
    public Flux<String> streamResponse(String userMessage, Long projectId) {
        Long userId = helperFunctions.getCurrentUserId();
        StringBuilder fullResponse = new StringBuilder();
        CodeGenerationTools codeGenerationTools = new CodeGenerationTools(projectFileService, projectId);
        chatSessionService.createChatSessionIfNotExists(projectId, userId);


        Map<String, Object> advisorParams = Map.of(
                "userId", userId,
                "projectId", projectId
        );
        log.info("Request in ChatResponse for userId: {} & projectId: {}", userId, projectId);

        AtomicReference<Long> startTime = new AtomicReference<>(System.currentTimeMillis());
        AtomicReference<Long> endTime = new AtomicReference<>(0L);

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
                })
                .doOnError(error -> log.error("Response streaming threw an error: "+error.getMessage()))
                .doOnComplete(() -> {
                    Schedulers.boundedElastic().schedule(  // as 3rd party api call involved, offloading to another thread
                            () -> {
                                ChatSession chatSession = chatSessionRepository.getReferenceById(
                                        new ChatSessionId(userId, projectId)
                                );
                                parseAndSaveFiles(fullResponse.toString(), projectId);
                                Long thoughTime = (endTime.get() - startTime.get())/1000;
                                parseLlmResponseAndSaveChats(fullResponse.toString(), chatSession, userMessage, thoughTime);
                            });
                })
                .retryWhen(Retry.max(0))
                .map(chatClientResponse ->
                        Objects.requireNonNull(chatClientResponse.getResult().getOutput().getText()));

    }


    private void parseLlmResponseAndSaveChats(String llmResponse, ChatSession chatSession, String userMessage, Long thoughTime) {

        if(chatSession == null) {
            log.error("ChatSession object is null");
            throw new RuntimeException("ChatSession is null");
        }

        ChatMessage userChatMessage = chatService.createChatMessage(chatSession, null, userMessage,
                MessageRole.USER, 0);
        chatMessageRepository.save(userChatMessage);

        ChatMessage llmChatMessage = chatService.createChatMessage(chatSession, null, null,
                MessageRole.ASSISTANT, 0);
        List<ChatEvent> chatEvents = responseParser.parseChatEvents(llmResponse, llmChatMessage);
        chatEvents.addFirst(ChatEvent.builder()
                        .chatMessage(llmChatMessage)
                        .type(ChatEventType.THOUGHT_TIME)
                        .sequence(0)
                        .content("Thought for " + thoughTime.toString()  + " s")
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
