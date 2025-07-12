package com.jigubangbang.chat_service.model;

import lombok.Data;

@Data
public class ChatGroupDto {
    private String id;
    private String groupType;
    private int groupId;
    private String userId;
    private String nickname;
    private String profileImage;
    private String joinedAt;
    private int isCreator;
}
