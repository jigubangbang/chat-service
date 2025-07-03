package com.jigubangbang.chat_service.service;

import com.jigubangbang.chat_service.model.ChatDescRequestDto;
import com.jigubangbang.chat_service.model.ChatGroupDto;
import com.jigubangbang.chat_service.model.ChatMsgDto;
import com.jigubangbang.chat_service.model.ChatRoomDto;
import com.jigubangbang.chat_service.mapper.ChatMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String groupType = chatRoomInfo.getGroupType();
        String groupId = chatRoomInfo.getGroupId();
        System.out.println(groupType + groupId);

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
        // 채팅방 멤버로 등록
        int isCreator = 0;

        // 채팅방 멤버로 추가
        ChatGroupDto newMember = new ChatGroupDto();
        newMember.setGroupType(groupType);
        newMember.setGroupId(Integer.parseInt(groupId));
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

    // 채팅 참여자 강제 탈퇴
    @Transactional
    public void removeGroupMember(Long chatId, String userId, String creatorId) {
        
        ChatGroupDto creator = chatMapper.getChatGroupMember(chatId, creatorId);

        if (creator == null || creator.getIsCreator() != 1) {
            throw new IllegalArgumentException("채팅방 관리자 권한이 필요합니다.");
        }

        Map<String, Object> targetParam = new HashMap<>();
        targetParam.put("chatId", chatId);
        targetParam.put("userId", userId);

        ChatGroupDto targetMember = chatMapper.getChatGroupMember(chatId, userId);

        if (targetMember == null) {
            throw new IllegalArgumentException("해당 사용자는 채팅방 멤버가 아닙니다.");
        }

        if (creatorId.equals(userId)) {
            throw new IllegalArgumentException("자기 자신을 강제 탈퇴시킬 수 없습니다.");
        }

        // 운영진은 내보낼 수 없게 만들어도 좋을듯

        chatMapper.removeGroupMemberByCreator(targetParam);
    }

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

    // 회원 운영진 승격
    public void promoteToAdmin(Long chatId, String userId) {
        ChatGroupDto targetMember = chatMapper.getChatGroupMember(chatId, userId);
        if (targetMember == null) {
            throw new IllegalArgumentException("해당 유저는 채팅방 멤버가 아닙니다.");
        }
        String groupType = targetMember.getGroupType();
        int groupId = targetMember.getGroupId();

        chatMapper.promoteToAdmin(groupType,groupId, userId);
    }

    // 운영진 제외
    public void demoteAdmin(Long chatId, String userId) {
        ChatGroupDto targetMember = chatMapper.getChatGroupMember(chatId, userId);
        if (targetMember == null) {
            throw new IllegalArgumentException("해당 유저는 채팅방 멤버가 아닙니다.");
        }
        String groupType = targetMember.getGroupType();
        int groupId = targetMember.getGroupId();

        chatMapper.demoteAdmin(groupType,groupId, userId);
    }

    // 채팅방 정보 수정
    public String updateDescription(Long chatId, ChatDescRequestDto newDescription, String requestorId) {
        ChatRoomDto chatRoom = chatMapper.getChatRoomInfo(chatId);
        if (chatRoom == null) {
            throw new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + chatId);
        }
        chatRoom.setDescription(newDescription.getDescription());
        chatMapper.saveDescription(chatRoom);
        return "설명이 수정되었습니다.";
    }

}
