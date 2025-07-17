package com.jigubangbang.chat_service.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private Long id;
    private String userId;
    private String type;
    private String title;
    private String message;

    private int relatedId;
    private String relatedType;
    private String relatedUrl;

    private String senderId;
    private String senderProfileImage;

    @JsonProperty("isRead")
    private boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
