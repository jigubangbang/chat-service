# 💬 Chat Service

**Jigubangbang 실시간 채팅 및 통합 알림 마이크로서비스**

이 서비스는 Jigubangbang MSA 아키텍처에서 **실시간 통신의 중심 허브** 역할을 담당하며,
모든 마이크로서비스의 알림을 통합 관리하는 핵심 인프라입니다.

## 📋 서비스 개요

### 🎯 주요 역할
- **실시간 채팅**: WebSocket/STOMP 기반 1:1 및 그룹 채팅
- **통합 알림 허브**: 8개 마이크로서비스의 알림을 중앙집중식 관리
- **MSA 통신 중계**: 서비스 간 실시간 이벤트 전파

### 🏗️ 아키텍처 위치
```
Frontend (React) ←→ API Gateway ←→ Chat Service ←→ [Other Services]
                                        ↕
                                   WebSocket/STOMP
                                        ↕
                                   Notification Hub
```

## 🚀 주요 기능

### 1. 실시간 채팅 시스템
- **WebSocket + STOMP** 기반 양방향 통신
- **1:1 및 그룹 채팅** 지원
- **실시간 메시지 브로드캐스트**
- **채팅방 관리** (입장/퇴장, 운영진 권한)
- **채팅 히스토리** 조회 및 관리

### 2. 통합 알림 시스템
**8가지 알림 타입 처리**
- **피드 관련**: 좋아요, 댓글, 팔로우
- **그룹 관련**: 신청, 수락, 강제퇴장  
- **시스템 관련**: 뱃지, 문의답변, 블라인드

**핵심 기능**
- 실시간 개인별 알림 전송
- 읽음/안읽음 상태 관리
- 알림 히스토리 관리
- 서비스 간 알림 API 제공

## 🛠️ 기술 스택

**Core Framework**
- Java 17 + Spring Boot 3.4.6
- Spring WebSocket + STOMP (실시간 통신)
- Spring Cloud 2024.0.1 (MSA 구성)

**Data & Persistence**
- MyBatis 3.x + MySQL 8.x
- HikariCP (커넥션 풀)

**MSA Components**
- Eureka Client (서비스 디스커버리)
- Feign Client (서비스 간 통신)
- Spring Cloud Config (설정 관리)

**Security**
- JWT Authentication
- Custom Feign Interceptor

## 🌐 API 엔드포인트

### REST API (사용자용)
```bash
# 채팅 관리
GET    /chat/rooms          # 채팅방 목록 조회
GET    /chat/rooms/{id}     # 채팅방 정보 조회
POST   /chat/rooms          # 채팅방 생성
GET    /chat/messages/{chatId}  # 채팅 히스토리 조회

# 알림 관리  
GET    /notifications       # 알림 목록 조회
PUT    /notifications/{id}/read  # 알림 읽음 처리
DELETE /notifications/{id}  # 알림 삭제
```

### WebSocket Endpoints
```bash
# 연결
CONNECT /ws/chat

# 구독 (수신)
SUBSCRIBE /topic/chat/{chatId}           # 채팅방 메시지
SUBSCRIBE /queue/notifications/{userId}  # 개인 알림

# 발행 (송신)
SEND /app/chat.send/{chatId}            # 메시지 전송
```

### Internal API (서비스간 통신)
```bash
# 알림 생성 (다른 서비스에서 호출)
POST /notifications/api/follow          # 팔로우 알림
POST /notifications/api/group-accepted  # 그룹 수락 알림
POST /notifications/api/badge-earned    # 뱃지 획득 알림
# ... 기타 8가지 알림 타입
```

## 🚀 실행 방법

1. **Config Server** 실행 (8888 포트)
2. **Eureka Server** 실행 (8761 포트)
3. **MySQL** 실행 및 DB 생성
4. **User Service** 실행 (Feign Client 의존성)
5.  **Chat Service 실행:**
    ```bash
    # chat-service 디렉토리로 이동하여 아래 명령어 실행
    ./mvnw spring-boot:run
    ```
6.  **API Gateway 실행:** 라우팅을 위해 API Gateway를 실행합니다.
7.  **프론트엔드 실행:** `msa-front` 디렉토리에서 WebSocket을 지원하는 클라이언트를 실행합니다.

### 로컬 실행
```bash
# 1. 프로젝트 클론
git clone https://github.com/jigubangbang/chat-service.git
cd chat-service

# 2. 의존성 설치 및 빌드
./mvnw clean install

# 3. 애플리케이션 실행
./mvnw spring-boot:run

# 4. 서비스 확인
curl http://localhost:8083/actuator/health
```

### Docker 실행
```bash
# Docker 이미지 빌드
docker build -t jigubangbang/chat-service .

# 컨테이너 실행
docker run -p 8083:8083 jigubangbang/chat-service
```

## 💡 주요 구현 사항

### 1. 실시간 메시지 동기화
**문제**: 다중 사용자 환경에서 메시지 순서 보장  
**해결**: STOMP 프로토콜과 트랜잭션 활용

```java
@MessageMapping("/chat.send/{chatId}")
public void sendMessage(@DestinationVariable Long chatId, @Payload ChatMsgDto dto) {
    dto.setChatId(chatId);
    chatService.sendMessage(dto);  // DB 저장 (트랜잭션)
    
    // 실시간 브로드캐스트
    messagingTemplate.convertAndSend("/topic/chat/" + chatId, responseDto);
}
```

### 2. MSA 인증 정보 전파
**문제**: Feign Client 호출 시 JWT 토큰 전달  
**해결**: Custom Feign Interceptor 구현

```java
@Override
public void apply(RequestTemplate template) {
    ServletRequestAttributes attributes = 
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
    if (attributes != null) {
        String authHeader = attributes.getRequest().getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            template.header("Authorization", authHeader);
        }
    }
}
```

### 3. 복잡한 알림 타입 관리
**문제**: 8개 서비스에서 오는 서로 다른 알림 형태  
**해결**: 타입별 전용 메서드 + 공통 패턴

```java
// 각 서비스별 맞춤 알림 생성
public void createFollowNotification(String authorId, ...)    // Feed Service
public void createGroupAccepted(String applicantId, ...)      // Com Service  
public void createBadgeEarned(String userId, ...)             // Quest Service
```

### 4. 복잡한 DB 조인 최적화
**문제**: 채팅방, 사용자, 그룹 정보의 복잡한 관계  
**해결**: MyBatis ResultMap과 조건부 조인

```xml
<select id="getChatRoomInfo" resultMap="chatRoomResultMap">
    SELECT cr.id, cr.group_type, cr.group_id, cr.description,
        CASE 
            WHEN cr.group_type = 'TRAVELMATE' THEN tm.title
            WHEN cr.group_type = 'TRAVELINFO' THEN ti.title  
            ELSE '알 수 없음'
        END as group_name
    FROM chat_room cr
    LEFT JOIN travelmate tm ON cr.group_id = tm.id AND cr.group_type = 'TRAVELMATE'
    LEFT JOIN travelinfo ti ON cr.group_id = ti.id AND cr.group_type = 'TRAVELINFO'
</select>
```

## 🔧 설정 정보

### 주요 포트
- **서비스 포트**: 8083
- **WebSocket**: ws://localhost:8083/ws/chat

### 연동 서비스
- **user-service**: 사용자 정보 조회
- **com-service**: 커뮤니티 관련 데이터
- **feed-service**: 피드 알림 요청
- **quest-service**: 뱃지 알림 요청
- **admin-service**: 문의 답변 알림

## 🚨 트러블슈팅

### WebSocket 연결 실패
```bash
# 증상: WebSocket 연결이 되지 않음
# 원인: CORS 설정 또는 JWT 토큰 문제
# 해결: 브라우저 개발자 도구에서 네트워크 탭 확인

# JWT 토큰 확인
curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8083/chat/rooms
```

### 강제 퇴장 시스템 구현
```bash
# 증상: WebSocket 연결 상태에서 강제 퇴장 시 세션 불일치
# 원인: WebSocket 세션과 채팅방 상태 관리 혼재
# 해결: 세션 관리 분리 + 특별 메시지 타입 활용

# 1. 강제 퇴장 메시지 전송 (서버 → 클라이언트)
// MessageType.FORCE_EXIT로 클라이언트에 알림

# 2. 세션 강제 종료 후 재연결 유도
// 서버 측 WebSocket 세션 종료
// 클라이언트 측 자동 재연결 처리

# 3. 실시간 참여자 목록 동기화
// 채팅방 참여자 목록 즉시 업데이트
```

### Feign Client 인증 오류
```bash
# 증상: 401 Unauthorized from user-service
# 원인: JWT 토큰 전파 실패
# 해결: FeignClientInterceptor 동작 확인 및 토큰 유효성 검증
```

## 👥 기여자

**개발 담당**: **이설영** (팀장)
- WebSocket/STOMP 실시간 통신 구현
- 통합 알림 시스템 설계 및 구현
- MSA 서비스 간 연동 로직
- JWT 인증 및 보안 처리

## 🔗 관련 리포지토리

- **전체 프로젝트**: [Jigubangbang Organization](https://github.com/jigubangbang)
- **인프라 서비스**: [infra-platform](https://github.com/jigubangbang/infra-platform)
- **사용자 서비스**: [user-admin-repo](https://github.com/jigubangbang/user-admin-repo)
- **프론트엔드**: [msa-front](https://github.com/jigubangbang/msa-front)

---

**💡 Quick Start**
```bash
# 전체 인프라가 실행된 상태에서
git clone https://github.com/jigubangbang/chat-service.git
cd chat-service && ./mvnw spring-boot:run

# WebSocket 테스트
wscat -c ws://localhost:8083/ws/chat
```

실시간 소통의 핵심을 담당하는 Chat Service와 함께 원활한 커뮤니케이션을 경험해보세요! 🎉
