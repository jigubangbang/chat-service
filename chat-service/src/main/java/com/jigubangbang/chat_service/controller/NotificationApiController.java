package com.jigubangbang.chat_service.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.jigubangbang.chat_service.model.NotificationDto;
import com.jigubangbang.chat_service.model.feign.BadgeNotificationRequestDto;
import com.jigubangbang.chat_service.model.feign.ComNotificationRequestDto;
import com.jigubangbang.chat_service.model.feign.FeedNotificationRequestDto;
import com.jigubangbang.chat_service.model.feign.GroupAcceptedNotificationRequestDto;
import com.jigubangbang.chat_service.model.feign.GroupApplyNotificationRequestDto;
import com.jigubangbang.chat_service.model.feign.InquiryNotificationRequestDto;
import com.jigubangbang.chat_service.service.NotificationApiService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/notifications")
public class NotificationApiController {

    @Autowired
    private NotificationApiService notificationService;

    // 범용 알림 생성
    @PostMapping
    public ResponseEntity<Map<String, Object>> createNotification(@RequestBody NotificationDto request) {
        
        try {
            Long notificationId = notificationService.createNotification(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "notificationId", notificationId,
                "message", "알림이 성공적으로 생성되었습니다."
            ));
        } catch (Exception e) {
            System.out.println("[NotificationAPI] 알림 생성 실패: " + e.getMessage() + e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "알림 생성에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    // FEED_SERVICE
    // 팔로우 알림
    @PostMapping("/feed/follow")
    public ResponseEntity<Map<String, Object>> createFollowNotification(@RequestBody FeedNotificationRequestDto request) {
        try {
            notificationService.createFollowNotification(
                request.getAuthorId(),
                request.getFeedId(),         // feed? user? : 이게 피드 관련이 아니면 UserNotificationRequestDto 새로 생성하여서 이 줄 없애기
                request.getRelatedUrl(),
                request.getSenderId(),
                request.getNickname(),
                request.getSenderProfileImage()
            );
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            System.out.println("댓글 알림 생성 실패" + e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 피드 좋아요 알림
    @PostMapping("/feed/like")
    public ResponseEntity<Map<String, Object>> createFeedLikeNotification(@RequestBody FeedNotificationRequestDto request) {
        try {
            notificationService.createFeedLike(
                request.getAuthorId(),
                request.getFeedId(),       
                request.getRelatedUrl(),
                request.getSenderId(),
                request.getNickname(),
                request.getSenderProfileImage()
            );
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            System.out.println("댓글 알림 생성 실패" + e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 피드 댓글 알림
    @PostMapping("/feed/comment")
    public ResponseEntity<Map<String, Object>> createFeedCommentNotification(@RequestBody FeedNotificationRequestDto request) {
        try {
            notificationService.createFeedComment(
                request.getAuthorId(),
                request.getFeedId(),    
                request.getRelatedUrl(),
                request.getSenderId(),
                request.getNickname(),
                request.getSenderProfileImage()
            );
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            System.out.println("댓글 알림 생성 실패" + e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 피드 게시글 알림 (추후)


    // COM_SERVICE
    // 게시물 댓글 알림
    @PostMapping("/posts/comment")
    public ResponseEntity<Map<String, Object>> createPostCommentNotification(@RequestBody ComNotificationRequestDto request) {
        try {
            notificationService.createPostComment(
                request.getAuthorId(),
                request.getPostId(),
                request.getRelatedUrl(),
                request.getSenderId(),
                request.getNickname(),
                request.getSenderProfileImage()
            );
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            System.out.println("댓글 알림 생성 실패" + e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }


    // 투표기한 만료 알림 (추후)
    /* 
    @PostMapping("/user-com/polls/{poll_id}/close")
    public ResponseEntity<Map<String, Object>> createVoteNotification(@PathVariable int pollId) {
        // DTO도 추가 안함
        try {
            notificationService.createVoteExpired(
        
        } catch (Exception e) {
            System.out.println("투표 만료 알림 실패" + e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    */

    // 그룹 가입 신청 알림
    @PostMapping("/travelgroup/applications")
    public ResponseEntity<Map<String, Object>> createGroupApplyNotification(@RequestBody GroupApplyNotificationRequestDto request) {
        try {
            notificationService.createNewApplicant(
                request.getCreatorId(),         // 방장에게
                request.getGroupName(),
                request.getGroupId(),      
                request.getRelatedUrl(),
                request.getNickname(),
                request.getApplicantId()        // 신청자가
            );
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            System.out.println("그룹 가입 신청 실패" + e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }


    // 그룹 가입 알림
    @PostMapping("/travelgroup/applications/{applicantId}")
    public ResponseEntity<Map<String, Object>> createGroupAcceptedNotification(@RequestBody GroupAcceptedNotificationRequestDto request) {
        try {
            notificationService.createGroupAccepted(
                request.getApplicantId(),       // 신청자에게
                request.getGroupName(),
                request.getGroupId(),
                request.getNickname(),     
                request.getRelatedUrl(),
                request.getCreatorId()          // 방장? system?
            );
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            System.out.println("그룹 멤버 추가 실패" + e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // QUEST_SERVICE
    // 뱃지 획득 알림
    @PostMapping("/quests/badges/earned")
    public ResponseEntity<Map<String, Object>> createBadgeEarnedNotification(@RequestBody BadgeNotificationRequestDto request) {
        try {
            notificationService.createBadgeEarned(
                request.getUserId(),            // 뱃지를 획득한 유저에게
                request.getBadgeName(),         // 뱃지 이름
                request.getBadgeId(),           // 뱃지 ID
                request.getRelatedUrl(),        // 뱃지 상세 페이지 또는 퀘스트 페이지
                request.getSenderId(),          // 시스템
                request.getNickname(),
                request.getSenderProfileImage()
            );
            return ResponseEntity.ok(Map.of("success", true, "message", "뱃지 획득 알림이 생성되었습니다."));
        } catch (Exception e) {
            System.out.println("[NotificationAPI] 뱃지 획득 알림 생성 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ADMIN_SERVICE
    // 뱃지 수거 알림 (관리자가 점검 후 뱃지 취소)
    @PostMapping("/admin/badges/revoked")
    public ResponseEntity<Map<String, Object>> createBadgeRevokedNotification(@RequestBody BadgeNotificationRequestDto request) {
        try {
            notificationService.createBadgeRevoked(
                request.getUserId(),            // 뱃지가 수거된 유저에게
                request.getBadgeName(),         // 뱃지 이름
                request.getMessage(),           // 수거 이유
                request.getBadgeId(),           // 뱃지 ID
                request.getRelatedUrl(),        // 관련 페이지
                request.getSenderId(),          // 관리자 또는 시스템
                request.getNickname(),
                request.getSenderProfileImage()
            );
            return ResponseEntity.ok(Map.of("success", true, "message", "뱃지 수거 알림이 생성되었습니다."));
        } catch (Exception e) {
            System.out.println("[NotificationAPI] 뱃지 수거 알림 생성 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 1:1 문의 답변/처리 알림
    @PostMapping("/inqury")
    public ResponseEntity<Map<String, Object>> createInquiryAnsweredNotification(@RequestBody InquiryNotificationRequestDto request) {
        try {
            notificationService.createInquiryAnswered(
                request.getUserId(),
                request.getMessage(),
                request.getRelatedUrl(),
                request.getSenderId()
            );
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            System.out.println("[NotificationAPI] 문의 답변 알림 생성 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 공지
    
}
