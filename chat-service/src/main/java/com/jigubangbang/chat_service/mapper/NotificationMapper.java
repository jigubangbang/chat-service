package com.jigubangbang.chat_service.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jigubangbang.chat_service.model.NotificationDto;

@Mapper
public interface NotificationMapper {
    
    void insertNotification(NotificationDto notification);

    List<NotificationDto> findUnreadNotifications(@Param("userId") String userId, @Param("limit") int limit);
    List<NotificationDto> findAllNotifications(@Param("userId") String userId, @Param("limit") int limit, @Param("offset") int offset);
    List<NotificationDto> findNotificationsSince(@Param("userId") String userId, @Param("limit") int limit, @Param("offset") int offset, @Param("since") LocalDateTime since);
    int countUnreadNotifications(String userId);
    
    void markAsRead(@Param("id") Long id, @Param("userId") String userId, @Param("readAt") LocalDateTime readAt);
    
    void markAllAsRead(@Param("userId")String userId, @Param("readAt") LocalDateTime readAt);
    
    void deleteNotification(@Param("id")Long id, @Param("userId") String userId);
    
    Map<String, Boolean> findNotificationSettings(String userId);
    
    // void updateNotificationSettings(@Param("userId") String userId, @Param("settings") Map<String, Boolean> settings);

}