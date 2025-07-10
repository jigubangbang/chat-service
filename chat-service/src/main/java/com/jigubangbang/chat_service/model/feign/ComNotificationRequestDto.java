package com.jigubangbang.chat_service.model.feign;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComNotificationRequestDto {

    private String authorId;                //userId
    private int postId;                     //relatedId
    private String relatedUrl;
    private String senderId;
    private String nickname;
    private String senderProfileImage;
    
}