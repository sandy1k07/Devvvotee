package com.springboot.projects.devvvotee.Service;

import com.springboot.projects.devvvotee.Dto.Chat.AiChatResponse;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface AiGenerativeService {
    Flux<AiChatResponse> streamResponse(String message, Long projectId);
}
