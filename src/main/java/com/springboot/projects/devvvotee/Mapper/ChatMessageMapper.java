package com.springboot.projects.devvvotee.Mapper;

import com.springboot.projects.devvvotee.Dto.Chat.ChatResponse;
import com.springboot.projects.devvvotee.Entity.ChatMessage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {
    ChatResponse toChatResponse(ChatMessage chatMessage);
}
