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
        // 이미 참여중인지 확인
        ChatGroupDto existingMember = chatMapper.getChatGroupMemberWithNickname(chatId, userId);
        if (existingMember == null) {
            throw new IllegalArgumentException("해당 그룹의 멤버가 아닙니다. 먼저 그룹에 가입해주세요.");
        }
        return existingMember;
    }

    // 채팅방 정보 조회
    public ChatRoomDto getChatRoomInfo( Long chatId ){
        return chatMapper.getChatRoomInfo(chatId);
    }

    public List<ChatGroupDto> getChatGroupMembers( Long chatId ) {
        return chatMapper.getChatGroupMembers(chatId);
    }

    // 최초 생성자 찾기 (신고용)
    public String findOriginalCreator(Long chatId) {
        List<ChatGroupDto> creators = chatMapper.getChatGroupCreators(chatId);
        
        if (creators.isEmpty()) {
            throw new IllegalArgumentException("채팅방의 생성자를 찾을 수 없습니다: " + chatId);
        }
        
        // joinedAt 기준으로 가장 이른 생성자 찾기
        ChatGroupDto originalCreator = creators.stream()
            .min((a, b) -> a.getJoinedAt().compareTo(b.getJoinedAt()))
            .orElseThrow(() -> new IllegalArgumentException("최초 생성자를 찾을 수 없습니다."));
        
        return originalCreator.getUserId();
    }

    // 채팅 메세지 조회
    public List<ChatMsgDto> getChatMessages(Long chatId) {
        return chatMapper.getChatMessages(chatId);
    }

    // 채팅 메세지 24시간 조회
    public List<ChatMsgDto> getRecentChatMessages(Long chatId, LocalDateTime since) {
        return chatMapper.getRecChatMessages(chatId, since);
    }

    // 닉네임 받아오기
    public String getUserNickname(Long chatId, String userId) {
        try {
            ChatGroupDto member = chatMapper.getChatGroupMemberWithNickname(chatId, userId);
            return member != null ? member.getNickname() : userId; // 닉네임이 없으면 userId 반환
        } catch (Exception e) {
            System.err.println("닉네임 조회 실패: " + e.getMessage());
            return userId; // 실패 시 userId 반환
        }
    }

    // 채팅 메세지 전송
    public void sendMessage(ChatMsgDto dto) {
        chatMapper.insertMessage(dto);
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

    }

    // 채팅방 삭제 (최초 생성자 또는 관리자만 가능)
    @Transactional
    public void deleteChatRoom(Long chatId, String userId) {
        // 채팅방 존재 확인
        ChatRoomDto chatRoom = chatMapper.getChatRoomInfo(chatId);
        if (chatRoom == null) {
            throw new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + chatId);
        }
        
        // 사용자가 해당 채팅방의 멤버인지 확인
        ChatGroupDto member = chatMapper.getChatGroupMember(chatId, userId);
        if (member == null) {
            throw new IllegalArgumentException("해당 채팅방의 멤버가 아닙니다.");
        }
        
        // 권한 확인: 최초 생성자 또는 관리자 권한 확인
        String originalCreator = findOriginalCreator(chatId);
        boolean isOriginalCreator = originalCreator.equals(userId);
        boolean isAdmin = member.getIsCreator() == 1; // 관리자 권한 확인
        
        if (!isOriginalCreator && !isAdmin) {
            throw new IllegalArgumentException("채팅방 삭제 권한이 없습니다. 최초 생성자 또는 관리자만 삭제할 수 있습니다.");
        }
        
        try {

            // 채팅 메시지 삭제
            chatMapper.deleteChatMessages(chatId);

            // 채팅방 멤버 정보 삭제
                // 삭제할 멤버 ID들 먼저 조회
                List<Long> memberIds = chatMapper.getChatGroupMemberIds(chatId);
                System.out.println("[DEBUG] 삭제할 멤버 수: " + memberIds.size());
                // Primary Key로 개별 삭제
                for (Long memberId : memberIds) {
                    chatMapper.deleteChatGroupMemberById(memberId);
                }
            // 채팅방 정보 삭제
            chatMapper.deleteChatRoom(chatId);
            
            System.out.println("채팅방 삭제 완료: chatId=" + chatId + ", deletedBy=" + userId);
            
        } catch (Exception e) {
            System.err.println("채팅방 삭제 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("채팅방 삭제 중 오류가 발생했습니다.", e);
        }
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
