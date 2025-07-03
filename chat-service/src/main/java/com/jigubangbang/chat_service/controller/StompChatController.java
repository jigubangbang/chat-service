package com.jigubangbang.chat_service.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.jigubangbang.chat_service.model.ChatMsgDto;
import com.jigubangbang.chat_service.model.ChatMsgResponseDto;
import com.jigubangbang.chat_service.service.StompChatService;

@Controller
public class StompChatController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private StompChatService chatService;

    @MessageMapping("/chat.addUser/{chatId}")
    public void addUser(@DestinationVariable Long chatId, @Payload ChatMsgDto chatMessage, SimpMessageHeaderAccessor headerAccessor) {
            headerAccessor.getSessionAttributes().put("userId", chatMessage.getSenderId());
            headerAccessor.getSessionAttributes().put("chatId", chatId);
            
            // 프론트엔드의 client.subscribe(`/topic/chat/{chatId}`)로 전송
            ChatMsgResponseDto joinMessage = new ChatMsgResponseDto();
            joinMessage.setChatId(chatId);
            joinMessage.setSenderId("System");
            joinMessage.setMessage(chatMessage.getSenderId() + " 님이 입장했습니다.");
            joinMessage.setType("ENTER");
            joinMessage.setCreatedAt(LocalDateTime.now());
            
            messagingTemplate.convertAndSend("/topic/chat/" + chatId, joinMessage);
        }

    // 클라이언트가 메시지 보낼 때: /app/chat.send
    @MessageMapping("/chat.send/{chatId}")
    public void sendMessage(@DestinationVariable Long chatId, @Payload ChatMsgDto dto) {
        try {
            dto.setChatId(chatId);
            chatService.sendMessage(dto);
            System.out.println("STOMP: Message from " + dto.getSenderId() + " saved and broadcasting to chat " + dto.getChatId());
            // 해당 채팅방으로 메시지 브로드캐스트
            messagingTemplate.convertAndSend("/topic/chat/" + dto.getChatId(), dto);
        } catch (Exception e) {
            System.err.println("STOMP: Failed to save or send message: " + e.getMessage());
            // 필요시 에러 메시지를 특정 사용자에게 전송할 수 있습니다.
            // messagingTemplate.convertAndSendToUser(chatMessage.getSenderId(), "/queue/errors", "메시지 전송 실패!");
        }
    }
}
