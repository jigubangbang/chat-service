package com.jigubangbang.chat_service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jigubangbang.chat_service.model.NotificationDto;
import com.jigubangbang.chat_service.service.NotificationService;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
 
    @Autowired
    private NotificationService notificationService;

    // 7일 이내 전체 알림 조회
    @GetMapping("/all")
    public ResponseEntity<List<NotificationDto>> getAllNotifications(@RequestHeader("User-Id") String userId, 
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size, @RequestParam(defaultValue = "0") int days) {
        List<NotificationDto> notifications = notificationService.getAllNotifications(userId, page, size, days);
        return ResponseEntity.ok(notifications);
    }

    // 읽지 않은 알림 조회
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(@RequestHeader("User-Id") String userId, @RequestParam(defaultValue = "20") int limit) {
        List<NotificationDto> notifications = notificationService.getUnreadNotifications(userId, limit);
        return ResponseEntity.ok(notifications);
    }

    // 읽지 않은 알림 개수 조회
    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(@RequestHeader("User-Id") String userId) {
        int count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }


    // 알림 읽음 처리
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @RequestHeader("User-Id") String userId) {
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    // 모든 알림 읽음 처리
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestHeader("User-Id") String userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    // 알림 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id, @RequestHeader("User-Id") String userId) {
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.ok().build();
    }

    // 알림 설정 조회 
    /* 
    @GetMapping("/settings")
    public ResponseEntity<Map<String, Boolean>> getNotificationSettings(@RequestHeader("User-Id") String userId) {
        Map<String, Boolean> settings = notificationService.getNotificationSettings(userId);
        return ResponseEntity.ok(settings);
    }
    */
}
