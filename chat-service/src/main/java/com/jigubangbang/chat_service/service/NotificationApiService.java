package com.jigubangbang.chat_service.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jigubangbang.chat_service.mapper.NotificationMapper;
import com.jigubangbang.chat_service.model.NotificationDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationApiService {
    
    private final NotificationMapper notificationMapper;

    @Transactional
    public Long createNotification(NotificationDto request) {
        NotificationDto notification = NotificationDto.builder()
            .userId(request.getUserId())
            .type(request.getType())
            .title(request.getTitle())
            .message(request.getMessage())
            .relatedId(request.getRelatedId())
            .relatedType(request.getRelatedType())
            .relatedUrl(request.getRelatedUrl())
            .senderId(request.getSenderId())
            .senderProfileImage(request.getSenderProfileImage())
            .build();
            
        notificationMapper.insertNotification(notification);
        return notification.getId();
    }

    // FEED_SERVICE
    @Transactional
    public void createFollowNotification(String authorId, int relatedId, String relatedUrl, 
            String senderId, String nickname, String senderProfileImage) {
        if (authorId.equals(senderId)) return;  // 자기 자신을 팔로우 X
    
        NotificationDto notification = NotificationDto.builder()
            .userId(authorId)
            .type("FOLLOW")
            .title("새 팔로워")
            .message(nickname + "(" + senderId + ")" + "님이 회원님을 팔로우했습니다.")
            .relatedId(relatedId)
            .relatedType("USER")
            .relatedUrl(relatedUrl)
            .senderId(senderId)
            .senderProfileImage(senderProfileImage)
            .build();
            
        notificationMapper.insertNotification(notification);
        System.out.println("팔로우 알림 생성: " +  senderId + " -> "  + authorId);
    }   

    @Transactional
    public void createFeedComment(String authorId, int relatedId, String relatedUrl, 
            String senderId, String nickname, String senderProfileImage) {
        if (authorId.equals(senderId)) return;  // 자신의 댓글 알림 X
        
        NotificationDto notification = NotificationDto.builder()
            .userId(authorId)
            .type("FEED_COMMENTED")
            .title("새 댓글")
            .message(nickname + "(" + senderId + ")" + "님이 회원님의 피드에 댓글을 남겼습니다.")
            .relatedId(relatedId)
            .relatedType("FEED")
            .relatedUrl(relatedUrl)
            .senderId(senderId)
            .senderProfileImage(senderProfileImage)
            .build();
            
        notificationMapper.insertNotification(notification);
        System.out.println("댓글 알림 생성: " + senderId + " -> " + authorId);
    }

    @Transactional
    public void createFeedLike(String authorId, int relatedId, String relatedUrl, 
            String senderId, String nickname, String senderProfileImage) {
        if (authorId.equals(senderId)) return;

        NotificationDto notification = NotificationDto.builder()
            .userId(authorId)
            .type("FEED_LIKED")
            .title("피드 좋아요")
            .message(nickname + "(" + senderId + ")" + "님이 회원님의 피드에 좋아요를 눌렀습니다.")
            .relatedId(relatedId)
            .relatedType("FEED")
            .relatedUrl(relatedUrl)
            .senderId(senderId)
            .senderProfileImage(senderProfileImage)
            .build();
            
        notificationMapper.insertNotification(notification);
        System.out.println("좋아요 알림 생성: " + senderId + " -> " + authorId);
    }

    // COM_SERVICE
    @Transactional
    public void createPostComment(String authorId, int relatedId, String relatedUrl, 
            String senderId, String nickname, String senderProfileImage) {
        if (authorId.equals(senderId)) return;  // 자신의 댓글 알림 X

        NotificationDto notification = NotificationDto.builder()
            .userId(authorId)
            .type("COM_COMMENTED")
            .title("새 댓글")
            .message(nickname + "(" + senderId + ")" + "님이 회원님의 커뮤니티 게시글에 댓글을 남겼습니다.")
            .relatedId(relatedId)
            .relatedType("COMMUNITY")
            .relatedUrl(relatedUrl)
            .senderId(senderId)
            .senderProfileImage(senderProfileImage)
            .build();
            
        notificationMapper.insertNotification(notification);
        System.out.println("댓글 알림 생성: " + senderId + " -> " + authorId);
    }


    /* 
    @Transactional
    public void createVoteExpired(String userId, Long voteId, String voteTitle) {
        NotificationDto notification = NotificationDto.builder()
            .userId(userId)
            .type("VOTE_EXPIRED")
            .title("투표 마감")
            .message("'" + voteTitle + "' 투표가 마감되었습니다.")
            .relatedId(voteId)
            .relatedType("VOTE")
            .relatedUrl("/votes/" + voteId + "/results")
            .senderId("system")
            .senderName("시스템")
            .build();
            
        notificationMapper.insertNotification(notification);
        System.out.println("투표 만료 알림 생성: " +  userId);
    }
    */

    @Transactional
    public void createNewApplicant(String creatorId, String groupName, int groupId,
            String relatedUrl, String applicantId, String nickname) {
        NotificationDto notification = NotificationDto.builder()
            .userId(creatorId)
            .type("GROUP_NEW_APPLICANT")
            .title("새 모임 신청")
            .message(nickname + "(" + applicantId + ") 님이 '" + groupName + "' 모임에 참가 신청했습니다.")
            .relatedId(groupId)
            .relatedType("GROUP")
            .relatedUrl(relatedUrl)
            .senderId(applicantId)
            .senderProfileImage(null)
            .build();
            
        notificationMapper.insertNotification(notification);
        System.out.println("새 신청자 알림 생성: " +  applicantId + " -> " + creatorId);
    }
    
    @Transactional
    public void createGroupAccepted(String applicantId, String groupName, int groupId,
            String relatedUrl, String creatorId, String nickname) {
        NotificationDto notification = NotificationDto.builder()
            .userId(applicantId)
            .type("GROUP_ACCEPTED")
            .title("모임 신청 수락")
            .message("'" + groupName + "' 모임 신청이 수락되었습니다.")
            .relatedId(groupId)
            .relatedType("GROUP")
            .relatedUrl(relatedUrl)
            .senderId(creatorId)
            .senderProfileImage(null)
            .build();
            
        notificationMapper.insertNotification(notification);
        System.out.println("모임 수락 알림 생성: " + creatorId + " -> " + applicantId);
    }

    @Transactional
    public void createInquiryAnswered(String userId, String message, String relatedUrl, String senderId) {
        NotificationDto notification = NotificationDto.builder()
            .userId(userId)
            .type("INQUIRY_ANSWERED")
            .title("문의 답변")
            .message(message)
            .relatedId(0)
            .relatedType("INQUIRY")
            .relatedUrl(relatedUrl)
            .senderId(senderId)
            .senderProfileImage(null)
            .build();
            
        notificationMapper.insertNotification(notification);
        System.out.println("문의 답변 알림 생성: " + userId);
    }

    @Transactional
    public void createBlind(String userId, String message, String relatedUrl, String senderId) {
        NotificationDto notification = NotificationDto.builder()
            .userId(userId)
            .type("BLIND_NOTIFICATION")
            .title("블라인드 처리")
            .message(message)
            .relatedId(0)
            .relatedType("BLIND")
            .relatedUrl(relatedUrl)
            .senderId(senderId)
            .senderProfileImage(null)
            .build();
            
        notificationMapper.insertNotification(notification);
        System.out.println("문의 답변 알림 생성: " + userId);
    }

    @Transactional
    public void createBadgeEarned(String userId, String badgeName, int badgeId, 
            String relatedUrl, String senderId, String nickname, String senderProfileImage) {
        NotificationDto notification = NotificationDto.builder()
            .userId(userId)
            .type("BADGE_EARNED")
            .title("뱃지 획득")
            .message("'" + badgeName + "' 뱃지를 획득했습니다!")
            .relatedId(badgeId)
            .relatedType("BADGE")
            .relatedUrl(relatedUrl)
            .senderId(senderId)
            .senderProfileImage(senderProfileImage)
            .build();
            
        notificationMapper.insertNotification(notification);
        System.out.println("뱃지 획득 알림 생성: " +  userId + " -> " + badgeName);
    }

    @Transactional
    public void createBadgeRevoked(String userId, String badgeName, String message, int badgeId, 
            String relatedUrl, String senderId, String nickname, String senderProfileImage) {
        NotificationDto notification = NotificationDto.builder()
            .userId(userId)
            .type("BADGE_REVOKED")
            .title("뱃지 수거")
            .message("제출하신 사진이 퀘스트와 불일치하여 " + badgeName + "을/를 회수하였습니다")
            .relatedId(badgeId)
            .relatedType("BADGE")
            .relatedUrl(relatedUrl)
            .senderId(senderId)
            .senderProfileImage(senderProfileImage)
            .build();
            
        notificationMapper.insertNotification(notification);
        System.out.println("뱃지 수거 알림 생성: " +  senderId + " -> " + userId);
    }

}
