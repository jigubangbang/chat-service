package com.jigubangbang.chat_service.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor
public class ChatMsgDto {
    private Long id;
    private Long chatId;
    private String senderId;
    private String nickname;
    private String message; 
    private LocalDateTime createdAt;
}
