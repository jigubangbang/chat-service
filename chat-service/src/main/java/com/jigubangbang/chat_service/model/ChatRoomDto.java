package com.jigubangbang.chat_service.model;

import lombok.Data;

@Data
public class ChatRoomDto {
    private Long id;
    private String groupType;
    private String groupId;
    private String description;
}
