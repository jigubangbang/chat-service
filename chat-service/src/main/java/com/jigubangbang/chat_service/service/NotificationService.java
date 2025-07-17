package com.jigubangbang.chat_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jigubangbang.chat_service.mapper.NotificationMapper;
import com.jigubangbang.chat_service.model.NotificationDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationMapper notificationMapper;

    public List<NotificationDto> getUnreadNotifications(String userId, int limit) {
        return notificationMapper.findUnreadNotifications(userId, limit);
    }
    
    public List<NotificationDto> getAllNotifications(String userId, int page, int size, int days) {
        int offset = page * size;
        if (days > 0) {
            // 특정 일수 이내의 알림만 조회
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            return notificationMapper.findNotificationsSince(userId, size, offset, since);
        } else {
            return notificationMapper.findAllNotifications(userId, size, offset);
        }
    }
    
    public int getUnreadCount(String userId) {
        return notificationMapper.countUnreadNotifications(userId);
    }
    
    @Transactional
    public void markAsRead(Long notificationId, String userId) {
        notificationMapper.markAsRead(notificationId, userId, LocalDateTime.now());
    }
    
    @Transactional
    public void markAllAsRead(String userId) {
        notificationMapper.markAllAsRead(userId, LocalDateTime.now());
    }
    
    @Transactional
    public void deleteNotification(Long notificationId, String userId) {
        notificationMapper.deleteNotification(notificationId, userId);
    }
    
    /*
    public Map<String, Boolean> getNotificationSettings(String userId) {
        return notificationMapper.findNotificationSettings(userId);
    }
     
    @Transactional
    public void updateNotificationSettings(String userId, Map<String, Boolean> settings) {
        notificationMapper.updateNotificationSettings(userId, settings);
    }
    */

}