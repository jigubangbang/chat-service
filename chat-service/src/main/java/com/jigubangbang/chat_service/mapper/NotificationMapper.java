package com.jigubangbang.chat_service.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jigubangbang.chat_service.model.NotificationDto;

@Mapper
public interface NotificationMapper {
    
    void insertNotification(NotificationDto notification);

    List<NotificationDto> findUnreadNotifications(String userId, int limit);
    List<NotificationDto> findAllNotifications(String userId, int limit, int offset);
    List<NotificationDto> findNotificationsSince(String userId, int limit, int offset, LocalDateTime since);
    int countUnreadNotifications(String userId);
    
    void markAsRead(Long id, String userId, LocalDateTime readAt);
    
    void markAllAsRead(String userId, LocalDateTime readAt);
    
    void deleteNotification(Long id, String userId);
    
    Map<String, Boolean> findNotificationSettings(String userId);
    
    // void updateNotificationSettings(@Param("userId") String userId, @Param("settings") Map<String, Boolean> settings);

}