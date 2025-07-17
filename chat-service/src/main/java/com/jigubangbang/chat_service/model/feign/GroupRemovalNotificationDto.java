package com.jigubangbang.chat_service.model.feign;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupRemovalNotificationDto {
    private String userId;             
    private String groupName;               //title
    private int groupId;                    //relatedId
    private String relatedUrl;
    private String creatorId;
}
