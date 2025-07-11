package com.jigubangbang.chat_service.controller;

import com.jigubangbang.chat_service.model.ChatDescRequestDto;
import com.jigubangbang.chat_service.model.ChatGroupDto;
import com.jigubangbang.chat_service.model.ChatMsgDto;
import com.jigubangbang.chat_service.model.ChatMsgResponseDto;
import com.jigubangbang.chat_service.model.ChatRoomDto;
import com.jigubangbang.chat_service.service.ChatService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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
    public ResponseEntity<ChatRoomDto> getChatRoomInfo(@PathVariable Long chatId) {
        ChatRoomDto InfoDto = chatService.getChatRoomInfo(chatId);
        return ResponseEntity.ok(InfoDto);
    }

    @GetMapping("{chatId}/members")
    public ResponseEntity<List<ChatGroupDto>> getChatGroupMembers(@PathVariable Long chatId) {
        List<ChatGroupDto> membersDto = chatService.getChatGroupMembers(chatId);
        return ResponseEntity.ok(membersDto);
    }

    /* messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/kicked",
            new KickMessage(chatId, "관리자에 의해 강퇴되었습니다.")
        ); 
    */

    // 채팅 참여자 강제 탈퇴
    @DeleteMapping("/{chatId}/members/{userId}")
    public ResponseEntity<Void> removeGroupMember(@PathVariable Long chatId, @PathVariable String userId, @RequestHeader("User-Id") String creatorId) {
        String userNickname = chatService.getUserNickname(chatId, userId);
        chatService.removeGroupMember(chatId, userId, creatorId);

        ChatMsgResponseDto leaveMessage = new ChatMsgResponseDto();
        leaveMessage.setChatId(chatId);
        leaveMessage.setSenderId("System");
        leaveMessage.setNickname("System");
        leaveMessage.setMessage( userNickname+ " 님이 강제 퇴장당했습니다.");
        leaveMessage.setType("LEAVE");
        leaveMessage.setCreatedAt(LocalDateTime.now());
        
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, leaveMessage);

        ChatMsgResponseDto kickMessage = new ChatMsgResponseDto();
        kickMessage.setChatId(chatId);
        kickMessage.setSenderId("System");
        leaveMessage.setNickname("System");
        kickMessage.setMessage("운영진에 의해 강제 퇴장 처리되었습니다.");
        kickMessage.setType("KICK");
        kickMessage.setCreatedAt(LocalDateTime.now());

        messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/kick/" + userId, kickMessage);
        System.out.println("개별 강퇴 알림 전송 완료: /topic/chat/" + chatId + "/kick/" + userId);
        
        return ResponseEntity.ok().build();
    }

    // 채팅방 나가기
    // 그룹 탈퇴하기
    @DeleteMapping("/{chatId}/members/me")
    public ResponseEntity<Void> leaveGroupMember(@PathVariable Long chatId, @RequestHeader("User-Id") String userId) {
        String userNickname = chatService.getUserNickname(chatId, userId);
        chatService.leaveGroupMember(chatId, userId);

        ChatMsgResponseDto leaveMessage = new ChatMsgResponseDto();
        leaveMessage.setChatId(chatId);
        leaveMessage.setSenderId("System");
        leaveMessage.setNickname("System");
        leaveMessage.setMessage( userNickname+ " 님이 그룹을 탈퇴했습니다.");
        leaveMessage.setType("LEAVE");
        leaveMessage.setCreatedAt(LocalDateTime.now());
        
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, leaveMessage);
        return ResponseEntity.ok().build();
    }

    // 회원 운영진 승격
    @PostMapping("/{chatId}/promote/{userId}")
    public ResponseEntity<Void> promoteToAdmin(@PathVariable Long chatId, @PathVariable String userId) {
        String userNickname = chatService.getUserNickname(chatId, userId);
        chatService.promoteToAdmin(chatId, userId);

        ChatMsgResponseDto promoteMessage = new ChatMsgResponseDto();
        promoteMessage.setChatId(chatId);
        promoteMessage.setSenderId("System");
        promoteMessage.setNickname("System");
        promoteMessage.setMessage(userNickname + " 님이 운영진으로 승격되었습니다.");
        promoteMessage.setType("SYSTEM");
        promoteMessage.setCreatedAt(LocalDateTime.now());
        
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, promoteMessage);

        return ResponseEntity.ok().build();
    }

    // 운영진 제외
    @PostMapping("/{chatId}/demote/{userId}")
    public ResponseEntity<Void> demoteAdmin(@PathVariable Long chatId, @PathVariable String userId) {
        String userNickname = chatService.getUserNickname(chatId, userId);
        chatService.demoteAdmin(chatId, userId);

        ChatMsgResponseDto demoteMessage = new ChatMsgResponseDto();
        demoteMessage.setChatId(chatId);
        demoteMessage.setSenderId("System");
        demoteMessage.setNickname("System");
        demoteMessage.setMessage(userNickname + " 님의 운영진 권한이 해제되었습니다.");
        demoteMessage.setType("SYSTEM");
        demoteMessage.setCreatedAt(LocalDateTime.now());
        
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, demoteMessage);

        return ResponseEntity.ok().build();
    }

    // 채팅방 정보 수정
    @PutMapping("/{chatId}/description")
    public ResponseEntity<String> updateDescription(@PathVariable Long chatId, @RequestBody ChatDescRequestDto request, @RequestHeader("User-Id") String userId) {
        chatService.updateDescription(chatId, request,userId);
        return ResponseEntity.ok("채팅방 설명이 수정되었습니다.");
    }

    // 타 서비스와 연동 함수
    // com-service
}
