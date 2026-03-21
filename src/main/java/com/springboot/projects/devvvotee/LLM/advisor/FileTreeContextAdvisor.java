package com.springboot.projects.devvvotee.LLM.advisor;

import com.springboot.projects.devvvotee.Dto.File.FileNode;
import com.springboot.projects.devvvotee.Service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileTreeContextAdvisor implements StreamAdvisor {  // working with streams so StreamAdvisor and not ChatAdvisor

    private final ProjectFileService  projectFileService;

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain streamAdvisorChain) {
        Map<String, Object> context = request.context(); // coming from advisorSpecParams
        Long projectId = (Long) context.get("projectId");
        ChatClientRequest augmentedRequest = augmentRequestWithFileTree(request, projectId);
        return streamAdvisorChain.nextStream(augmentedRequest);
    }

    private ChatClientRequest augmentRequestWithFileTree(ChatClientRequest request, Long projectId) {
        log.info("Incoming request for augmenting file tree for ProjectId: {}", projectId);
        List<Message> incomingMessage = request.prompt().getInstructions();

        Message systemMessage = incomingMessage.stream()
                .filter(msg -> msg.getMessageType() == MessageType.SYSTEM)
                .findFirst()
                .orElse(null);

        List<Message> userMessages = incomingMessage.stream()
                .filter(msg -> msg.getMessageType() != MessageType.SYSTEM)
                .toList();

        List<Message> allMessages = new ArrayList<>();
        if(systemMessage != null) { allMessages.add(systemMessage); } // ensuring our system msg is in beginning for caching

        List<FileNode> fileTree = projectFileService.getFileTree(projectId);
        String fileTreeContext = "\n ---- FILE TREE ---- \n" + fileTree.toString();
        allMessages.add(new SystemMessage(fileTreeContext));
        allMessages.addAll(userMessages);

        log.info("File tree augmented for projectId: {}", projectId);

        return request
                .mutate()
                .prompt(new Prompt(allMessages, request.prompt().getOptions()))  // get options has model name, temperature, like all the settings for LLM
                .build();
    }

    @Override
    public String getName() {
        return "FileTreeContextAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
