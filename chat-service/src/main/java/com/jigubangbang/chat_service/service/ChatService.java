package com.jigubangbang.chat_service.service;

import com.jigubangbang.chat_service.model.ChatGroupDto;
import com.jigubangbang.chat_service.model.ChatMsgDto;
import com.jigubangbang.chat_service.model.ChatRoomDto;
import com.jigubangbang.chat_service.model.ChatRoomMemberDto;
import com.jigubangbang.chat_service.model.TravelInfoDto;
import com.jigubangbang.chat_service.model.TravelmateDto;
import com.jigubangbang.chat_service.user_service.UserServiceClient;

import lombok.RequiredArgsConstructor;

import com.jigubangbang.chat_service.mapper.ChatMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final ChatMapper chatMapper;

    // 채팅방 입장
    @Transactional
    public ChatGroupDto joinChatRoom(Long chatId, String userId) {
        ChatRoomDto chatRoomInfo = chatMapper.getChatRoomInfo(chatId);
        if (chatRoomInfo == null) {
            throw new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + chatId);
        }
        // 이미 참여중인지 확인
        ChatGroupDto existingMember = chatMapper.getChatGroupMember(chatId, userId);
        if (existingMember != null) {
            return existingMember; // 이미 참여중이면 기존 정보 반환
        }
        // 사용자 정보 검증 (user-service 연동)
        /* 
        try {
            UserServiceClient.getUserById(userId);
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 사용자입니다: " + userId);
        }
        */
        // 채팅방 관리자 확인
        int isCreator = 0;
        // String isCreator = existingMember.getIsCreator();
        String groupType = chatRoomInfo.getGroupType();
        try {
            if (existingMember != null && existingMember.getIsCreator() == 1) {
                isCreator = 1;
            }
        } catch (Exception e) {
            System.err.println("com-service 연동 실패, 일반 멤버로 처리: " + e.getMessage());
        }
        // 채팅방 멤버로 추가
        ChatGroupDto newMember = new ChatGroupDto();
        newMember.setGroupType(groupType);
        newMember.setGroupId(chatId.intValue());
        newMember.setUserId(userId);
        newMember.setIsCreator(isCreator);
        
        chatMapper.insertGroupMember(newMember);
        
        return chatMapper.getChatGroupMember(chatId, userId);
    }

    // 채팅방 정보 조회
    public ChatRoomDto getChatRoomInfo( Long chatId ){
        return chatMapper.getChatRoomInfo(chatId);
    }

    public List<ChatGroupDto> getChatGroupMembers( Long chatId ) {
        return chatMapper.getChatGroupMembers(chatId);
    }

    // 채팅 메세지 조회
    public List<ChatMsgDto> getChatMessages(Long chatId) {
        return chatMapper.getChatMessages(chatId);
    }

    // 채팅 메세지 24시간 조회
    public List<ChatMsgDto> getRecentChatMessages(Long chatId, LocalDateTime since) {
        return chatMapper.getRecChatMessages(chatId, since);
    }

    /* 
    // 채팅 참여자 강제 탈퇴
    @Transactional
    public void removeGroupMember(Long chatId, String userId, String CreatorId) {
        // 관리자 권한 확인 - ChatGroupDto의 isCreator 필드 사용
        ChatGroupDto creatorId = chatMapper.getChatRoomMember(chatId, CreatorId);
        if (creatorId == null || !"Y".equals(creatorId.getIsCreator())) {
            throw new IllegalArgumentException("채팅방 관리자 권한이 필요합니다.");
        }
        // 대상 멤버 존재 여부 확인
        ChatGroupDto targetMember = chatMapper.getChatRoomMember(chatId, userId);
        if (targetMember == null) {
            throw new IllegalArgumentException("해당 사용자는 채팅방 멤버가 아닙니다.");
        }
        // 자기 자신은 강제 탈퇴시킬 수 없음
        if (creatorId.equals(userId)) {
            throw new IllegalArgumentException("자기 자신을 강제 탈퇴시킬 수 없습니다.");
        }
        // 멤버 제거
        chatMapper.removeGroupMemberByCreator(chatId, userId);
    }
    */

    // 채팅방 나가기
    private void validateChatRoomMember(Long chatId, String userId) {
        ChatGroupDto member = chatMapper.getChatGroupMember(chatId, userId);
        if (member == null) {
            throw new IllegalArgumentException("해당 사용자는 채팅방의 멤버가 아닙니다. chatId=" + chatId + ", userId=" + userId);
        }
    }

    @Transactional
    public void leaveGroupMember(Long chatId, String userId) {

        validateChatRoomMember(chatId, userId);
        chatMapper.leaveGroupMemberByUser(chatId, userId);
        
        // 채팅방에 남은 멤버가 없으면 채팅방 삭제 (선택사항)
        /* 
        List<ChatRoomMemberDto> remainingMembers = chatMapper.getChatRoomMembersByChatId(chatId);
        if (remainingMembers.isEmpty()) {
            chatMapper.deleteChatRoom(chatId);
        }
        */
    }
}
