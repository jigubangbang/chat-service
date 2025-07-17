package com.jigubangbang.chat_service.model.feign;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryNotificationRequestDto {
    private String userId;
    private String message;
    private String relatedUrl;
    private String senderId;
}
