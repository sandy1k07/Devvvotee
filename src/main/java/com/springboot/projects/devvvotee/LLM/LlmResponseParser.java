package com.springboot.projects.devvvotee.LLM;

import com.springboot.projects.devvvotee.Entity.ChatEvent;
import com.springboot.projects.devvvotee.Entity.ChatMessage;
import com.springboot.projects.devvvotee.enums.ChatEventType;
import com.springboot.projects.devvvotee.enums.LlmMessageAttribute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class LlmResponseParser {

    // breaking down the whole response corresponding to tags
    private static final Pattern GENERIC_TAG_PATTERN = Pattern.compile(
            "(<(message|file)([^>]*)>)([\\s\\S]*?)(</\\2>)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    // getting attributes like phase = "planning" for <message>, and path = "..." for <file>
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(
            "(path|phase)=\"([^\"]+)\""
    );

    public List<ChatEvent> parseChatEvents(String fullResponse, ChatMessage parentMessage) {
        List<ChatEvent> events = new ArrayList<>();
        int sequenceCounter = 1;

        Matcher matcher = GENERIC_TAG_PATTERN.matcher(fullResponse);

        while (matcher.find()) {
            String tagName = matcher.group(2).toLowerCase();
            String attributes = matcher.group(3);
            String content = matcher.group(4).trim();

            Map<String, String> attrMap = extractAttributes(attributes); // getting the attributes eg: phase = "planning"

            ChatEvent.ChatEventBuilder builder = ChatEvent.builder()
                    .chatMessage(parentMessage)
//                    .content(content)
                    .sequence(sequenceCounter++);

            switch (tagName) {
                case "message" -> {
                    builder.type(ChatEventType.MESSAGE);
                    builder.content(content);
                    builder.messageAttribute(mapMessageAttribute(attrMap.get("phase")));
                }
                case "file" -> {
                    builder.type(ChatEventType.FILE_EDIT);
                    builder.filePath(attrMap.get("path"));
                }
                default -> { continue; }
            }

            events.add(builder.build());
        }

        return events;
    }

    private LlmMessageAttribute mapMessageAttribute(String phase) {
        return switch (phase) {
            case "start" -> LlmMessageAttribute.START;
            case "planning" -> LlmMessageAttribute.PLANNING;
            case "completed" -> LlmMessageAttribute.COMPLETED;
            default -> {
                log.error("Invalid phase: {}", phase);
                yield null;
            }
        };
    }

    private Map<String, String> extractAttributes(String attributeString) {
        Map<String, String> attributes = new HashMap<>();
        if (attributeString == null) return attributes;

        Matcher matcher = ATTRIBUTE_PATTERN.matcher(attributeString);
        while (matcher.find()) {
            attributes.put(matcher.group(1).toLowerCase(), matcher.group(2));
        }
        return attributes;
    }

}
