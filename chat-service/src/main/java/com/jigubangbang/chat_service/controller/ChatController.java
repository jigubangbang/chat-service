package com.jigubangbang.chat_service.controller;

import com.jigubangbang.chat_service.mapper.ChatMapper;
import com.jigubangbang.chat_service.model.ChatGroupDto;
import com.jigubangbang.chat_service.model.ChatMsgDto;
import com.jigubangbang.chat_service.model.ChatRoomDto;
import com.jigubangbang.chat_service.service.ChatService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // 확인용(의미 X)
    @GetMapping("/{chatId}/join")
    public ResponseEntity<ChatGroupDto> getChatRoom(@PathVariable Long chatId,  @RequestHeader(value = "User-Id", required = false) String userId) {
        if (userId == null || userId.isEmpty()) {
            userId = "aaa"; // 테스트용 기본값
        }
        ChatGroupDto memberDto = chatService.joinChatRoom(chatId, userId);
        return ResponseEntity.ok(memberDto);
    }

    // 채팅방 입장
    @PostMapping("/{chatId}/join")
    public ResponseEntity<ChatGroupDto> joinChatRoom(@PathVariable Long chatId,  @RequestHeader("User-Id") String userId) {
        ChatGroupDto memberDto = chatService.joinChatRoom(chatId, userId);
        return ResponseEntity.ok(memberDto);
    }

    // 채팅 메세지 조회
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<ChatMsgDto>> getChatMessages(@PathVariable Long chatId) {
        return ResponseEntity.ok(chatService.getChatMessages(chatId));
    }

    // 채팅 메세지 전송
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<Void> sendMessage(@PathVariable Long chatId, @RequestBody ChatMsgDto dto) {
        System.out.println("받은 chatId: " + chatId);
        System.out.println("받은 dto: " + dto.toString()); 

        try {
            dto.setChatId(chatId);
            chatService.sendMessage(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
        e.printStackTrace(); // 에러 로그 출력
        throw e;
        }
    }

    // 채팅방 멤버 조회 - 확인용(의미 X)
    /* 
    @GetMapping("{chatId}/members")
    public String getChatRoomMembers(@RequestParam Long chatId, @RequestHeader("User-Id") String userId) {
        return ResponseEntity.ok(chatMapper.getChatRoomMember(userId));
    }
    */

    /* 
    // 채팅 참여자 강제 탈퇴
    @DeleteMapping("/{chatId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long chatId, @PathVariable Long userId, @RequestHeader("User-Id") String adminUserId) {
        chatService.removeMemberFromChatRoom(chatId, userId, adminUserId);
        return ResponseEntity.ok().build();
    }

    // 채팅방 나가기
    @DeleteMapping("/{chatId}/members/me")
    public ResponseEntity<Void> leaveChatRoom(@PathVariable Long chatId, @RequestHeader("User-Id") String userId) {
        chatService.leaveChatRoom(chatId, userId);
        return ResponseEntity.ok().build();
    }

    // 타 서비스와 연동 함수
    // com-service0
    */
}
