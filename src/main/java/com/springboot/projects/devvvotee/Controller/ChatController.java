package com.springboot.projects.devvvotee.Controller;

import com.springboot.projects.devvvotee.Dto.Chat.AiChatResponse;
import com.springboot.projects.devvvotee.Dto.Chat.ChatRequest;
import com.springboot.projects.devvvotee.Dto.Chat.ChatResponse;
import com.springboot.projects.devvvotee.Security.SecurityExpression;
import com.springboot.projects.devvvotee.Service.AiGenerativeService;
import com.springboot.projects.devvvotee.Service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatController {

    private final AiGenerativeService aiGenerativeService;
    private final ChatService chatService;

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<AiChatResponse>> streamChat(@RequestBody ChatRequest request) {  // server will send stream of data
        log.info("Received request {}", request);
        return aiGenerativeService.streamResponse(request.message(), request.projectId())
                .map(response -> ServerSentEvent.<AiChatResponse>builder()
                        .data(response)
                        .build());
    }

    @GetMapping("/chat/projects/{projectId}")
    public ResponseEntity<List<ChatResponse>> getChats(
            @PathVariable("projectId") Long projectId) {
        return ResponseEntity.ok(chatService.getProjectChats(projectId));
    }
}
