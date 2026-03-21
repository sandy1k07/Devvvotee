package com.springboot.projects.devvvotee.Service;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface AiGenerativeService {
    Flux<String> streamResponse(String message, Long projectId);
}
