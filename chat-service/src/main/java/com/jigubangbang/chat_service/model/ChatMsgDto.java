package com.jigubangbang.chat_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor
public class ChatMsgDto {
    private Long id;
    private Long chatId;
    private String senderId;
    private String message; 
    private LocalDateTime createdAt;
}
