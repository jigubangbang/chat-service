package com.jigubangbang.chat_service.model.feign;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedNotificationRequestDto {

    private String authorId;                //userId
    private int feedId;                     //relatedId
    private String relatedUrl;
    private String senderId;
    private String senderProfileImage;

}
