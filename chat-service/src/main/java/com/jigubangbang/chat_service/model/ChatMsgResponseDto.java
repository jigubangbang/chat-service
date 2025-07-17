package com.jigubangbang.chat_service.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMsgResponseDto {
    private Long chatId;
    private String senderId;
    private String nickname;
    private String message;
    private String type; // "ENTER", "TALK", "LEAVE"
    private LocalDateTime createdAt;
}
