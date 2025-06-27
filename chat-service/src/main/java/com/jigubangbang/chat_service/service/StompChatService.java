package com.jigubangbang.chat_service.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.jigubangbang.chat_service.mapper.ChatMapper;
import com.jigubangbang.chat_service.model.ChatGroupDto;
import com.jigubangbang.chat_service.model.ChatMsgDto;

@Service
@RequiredArgsConstructor
public class StompChatService {

    private final ChatMapper chatMapper;

    // 채팅 메세지 전송
    public void sendMessage(ChatMsgDto dto) {
        chatMapper.insertMessage(dto);
    }

    /* 
    // 클라이언트 입장할 때 호출 가능
    public void enter(ChatGroupDto enterUser) {
        // 1. DB에 참여자 정보 저장 (group_user 테이블 등)
        if (!chatMapper.isAlreadyInChatGroup(enterUser)) {
            chatMapper.insertUserIntoChatGroup(enterUser);
        }
        // 2. 입장 기록 로그 남기기 (선택)
        chatMapper.insertSystemMessage(enterUser.getGroupId(), enterUser.getUserId() + "님이 입장했습니다.");
    }
    */
}
