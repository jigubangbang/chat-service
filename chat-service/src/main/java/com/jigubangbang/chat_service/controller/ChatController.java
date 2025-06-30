package com.jigubangbang.chat_service.controller;

import com.jigubangbang.chat_service.model.ChatGroupDto;
import com.jigubangbang.chat_service.model.ChatMsgDto;
import com.jigubangbang.chat_service.model.ChatRoomDto;
import com.jigubangbang.chat_service.service.ChatService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // 채팅방 입장
    @PostMapping("/{chatId}/join")
    public ResponseEntity<ChatGroupDto> joinChatRoom(@PathVariable Long chatId,  @RequestHeader("User-Id") String userId) {
        ChatGroupDto memberDto = chatService.joinChatRoom(chatId, userId);
        return ResponseEntity.ok(memberDto);
    }

    // 채팅 메세지 조회
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<ChatMsgDto>> getChatMessages(@PathVariable Long chatId) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<ChatMsgDto> messages = chatService.getRecentChatMessages(chatId, since);
        return ResponseEntity.ok(messages);
        //return ResponseEntity.ok(chatService.getChatMessages(chatId));
    }

    // 채팅방 정보 조회
    @GetMapping("{chatId}/info")
    public ResponseEntity<ChatRoomDto> getChatRoomInfor(@PathVariable Long chatId) {
        ChatRoomDto InfoDto = chatService.getChatRoomInfo(chatId);
        return ResponseEntity.ok(InfoDto);
    }

    @GetMapping("{chatId}/members")
    public ResponseEntity<List<ChatGroupDto>> getChatGroupMembers(@PathVariable Long chatId) {
        List<ChatGroupDto> membersDto = chatService.getChatGroupMembers(chatId);
        return ResponseEntity.ok(membersDto);
    }

    /* 
    // 채팅 참여자 강제 탈퇴
    @DeleteMapping("/{chatId}/members/{userId}")
    public ResponseEntity<Void> removeGroupMember(@PathVariable Long chatId, @PathVariable Long userId, @RequestHeader("User-Id") String adminUserId) {
        chatService.removeGroupMember(chatId, userId, adminUserId);
        return ResponseEntity.ok().build();
    }
    */

    // 채팅방 나가기
    // 그룹 탈퇴하기
    @DeleteMapping("/{chatId}/members/me")
    public ResponseEntity<Void> leaveGroupMember(@PathVariable Long chatId, @RequestHeader("User-Id") String userId) {
        chatService.leaveGroupMember(chatId, userId);
        return ResponseEntity.ok().build();
    }

    // 타 서비스와 연동 함수
    // com-service0
}
