# Chat Service Repository

이 레포지토리는 Jigubangbang 프로젝트의 채팅 및 알림 관련 마이크로서비스를 관리합니다.

## 개요

MSA(Microservice Architecture) 구조에 따라 실시간 채팅과 사용자 알림 기능을 담당하는 서비스입니다. WebSocket과 STOMP 프로토콜을 사용하여 클라이언트와 실시간 양방향 통신을 지원합니다.

-   **Chat Service:** 1:1 채팅, 그룹 채팅, 채팅방 관리, 이전 메시지 조회 등 채팅 관련 모든 기능을 담당합니다.
-   **Notification Service:** 새로운 메시지, 친구 추가, 공지 등 다양한 유형의 실시간 알림을 사용자에게 전송하는 기능을 담당합니다.

## 주요 기술 스택

-   **Backend:** Java 17, Spring Boot 3, Spring Cloud, MyBatis, MySQL, Lombok, Feign Client
-   **Real-time:** Spring WebSocket, STOMP (Simple Text Oriented Messaging Protocol)
-   **Infra:** Eureka, Spring Cloud Gateway, Spring Cloud Config

---

## Chat & Notification Service

### 주요 기능

-   **실시간 채팅:** WebSocket과 STOMP를 기반으로 1:1 및 그룹 채팅 기능을 제공합니다. 사용자는 구독 중인 채팅방으로 들어오는 메시지를 실시간으로 수신할 수 있습니다.
-   **채팅방 관리:** 사용자는 새로운 채팅방을 생성하거나 기존 채팅방 목록을 조회할 수 있습니다.
-   **메시지 조회:** 특정 채팅방의 이전 대화 내역을 조회할 수 있습니다.
-   **실시간 알림:** 다른 서비스(피드, 친구, 문의 등)에서 발생하는 이벤트를 Feign Client로 수신하여, WebSocket을 통해 특정 사용자에게 실시간 알림을 전송합니다.
-   **알림 관리:** 사용자는 수신한 알림 목록을 조회하고, 개별 또는 전체 알림을 읽음 처리할 수 있습니다.

### API Endpoints

#### User-Facing REST API

| Method      | URL                                    | Role | 설명                               |
| :---------- | :------------------------------------- | :--- | :--------------------------------- |
| `POST`      | `/api/chat/{chatId}/join`              | USER | 채팅방 입장                        |
| `GET`       | `/api/chat/{chatId}/messages`          | USER | 채팅방 최근 메시지 조회 (24시간)   |
| `GET`       | `/api/chat/{chatId}/info`              | USER | 채팅방 정보 조회                   |
| `GET`       | `/api/chat/{chatId}/members`           | USER | 채팅방 참여자 목록 조회            |
| `GET`       | `/api/chat/{chatId}/original-creator`  | USER | 채팅방 최초 생성자 조회            |
| `DELETE`    | `/api/chat/{chatId}/members/{userId}`  | USER | (운영진) 참여자 강제 탈퇴          |
| `DELETE`    | `/api/chat/{chatId}/members/me`        | USER | 채팅방 나가기                      |
| `POST`      | `/api/chat/{chatId}/promote/{userId}`  | USER | (운영진) 운영진으로 승격           |
| `POST`      | `/api/chat/{chatId}/demote/{userId}`   | USER | (운영진) 운영진 권한 해제          |
| `PUT`       | `/api/chat/{chatId}/description`       | USER | (운영진) 채팅방 설명 수정          |
| `GET`       | `/api/notifications/all`               | USER | 내 전체 알림 목록 조회 (페이징)    |
| `GET`       | `/api/notifications/unread`            | USER | 읽지 않은 알림 목록 조회           |
| `GET`       | `/api/notifications/unread-count`      | USER | 읽지 않은 알림 개수 조회           |
| `POST`      | `/api/notifications/{id}/read`         | USER | 특정 알림 읽음 처리                |
| `POST`      | `/api/notifications/read-all`          | USER | 모든 알림 읽음 처리                |
| `DELETE`    | `/api/notifications/{id}`              | USER | 특정 알림 삭제                     |

#### Internal REST API (Called by other services)

| Method | URL                                      | Calling Service | 설명                         |
| :----- | :--------------------------------------- | :-------------- | :--------------------------- |
| `POST` | `/api/notifications`                     | (All)           | 범용 알림 생성               |
| `POST` | `/api/notifications/feed/follow`         | Feed Service    | 팔로우 알림 생성             |
| `POST` | `/api/notifications/feed/like`           | Feed Service    | 피드 좋아요 알림 생성        |
| `POST` | `/api/notifications/feed/comment`        | Feed Service    | 피드 댓글 알림 생성          |
| `POST` | `/api/notifications/posts/comment`       | Com Service     | 게시물 댓글 알림 생성        |
| `POST` | `/api/notifications/travelgroup/applications` | Com Service     | 그룹 가입 신청 알림 생성     |
| `POST` | `/api/notifications/travelgroup/applications/{applicantId}` | Com Service     | 그룹 가입 수락 알림 생성     |
| `POST` | `/api/notifications/quests/badges/earned` | Quest Service   | 뱃지 획득 알림 생성          |
| `POST` | `/api/notifications/admin/badges/revoked`| Admin Service   | 뱃지 수거 알림 생성          |
| `POST` | `/api/notifications/inquiry`             | Admin Service   | 1:1 문의 답변 알림 생성      |
| `POST` | `/api/notifications/blind`               | Admin Service   | 블라인드 처리 알림 생성      |

#### WebSocket (STOMP) Endpoints

| Destination                      | Type        | 설명                               |
| :------------------------------- | :---------- | :--------------------------------- |
| `/app/chat.addUser/{chatId}`     | `PUBLISH`   | 채팅방 입장 알림 (System 메시지)   |
| `/app/chat.send/{chatId}`        | `PUBLISH`   | 특정 채팅방으로 메시지 전송        |
| `/topic/chat/{chatId}`           | `SUBSCRIBE` | 특정 채팅방 구독 (메시지 수신)     |
| `/topic/chat/{chatId}/kick/{userId}` | `SUBSCRIBE` | (개인) 강제 퇴장 알림 수신         |
| `/queue/notifications/{userId}`  | `SUBSCRIBE` | 개인 알림 구독 (알림 수신)         |

### 데이터베이스 스키마

#### `chat_room`

채팅방 정보를 저장하는 테이블입니다.

| Column      | Type         | Description                        |
| :---------- | :----------- | :--------------------------------- |
| `id`        | BIGINT (PK, AI) | 채팅방 고유 ID                     |
| `group_type`| VARCHAR(255) | 채팅방 타입 (e.g., 'POST', 'QUEST') |
| `group_id`  | VARCHAR(255) | 관련 그룹 ID (e.g., 게시글 ID)     |
| `description`| VARCHAR(255) | 채팅방 설명                        |

#### `chat_message`

채팅 메시지를 저장하는 테이블입니다.

| Column     | Type        | Description                        |
| :--------- | :---------- | :--------------------------------- |
| `id`       | BIGINT (PK, AI) | 메시지 고유 ID                     |
| `chat_id`  | BIGINT (FK) | `chat_room`의 ID                   |
| `sender_id`| VARCHAR(255)| 발신자 ID                          |
| `nickname` | VARCHAR(255)| 발신자 닉네임                      |
| `message`  | TEXT        | 메시지 내용                        |
| `created_at`| TIMESTAMP   | 메시지 발송 시각                   |

#### `notification`

사용자 알림을 저장하는 테이블입니다.

| Column               | Type         | Description                        |
| :------------------- | :----------- | :--------------------------------- |
| `id`                 | BIGINT (PK, AI) | 알림 고유 ID                       |
| `user_id`            | VARCHAR(255) | 알림을 수신할 사용자 ID            |
| `type`               | VARCHAR(255) | 알림 타입 (e.g., 'CHAT', 'FRIEND') |
| `title`              | VARCHAR(255) | 알림 제목                          |
| `message`            | VARCHAR(255) | 알림 내용                          |
| `related_id`         | INT          | 관련 콘텐츠 ID                     |
| `related_type`       | VARCHAR(255) | 관련 콘텐츠 타입                   |
| `related_url`        | VARCHAR(255) | 클릭 시 이동할 URL                 |
| `sender_id`          | VARCHAR(255) | 발신자 ID                          |
| `sender_profile_image`| VARCHAR(255) | 발신자 프로필 이미지 URL           |
| `is_read`            | BOOLEAN      | 읽음 여부                          |
| `read_at`            | TIMESTAMP    | 읽은 시각                          |
| `created_at`         | TIMESTAMP    | 생성 시각                          |
| `updated_at`         | TIMESTAMP    | 수정 시각                          |

## 실행 방법

1.  **Config Server 실행:** 설정 정보를 가져오기 위해 Config Server를 먼저 실행해야 합니다.
2.  **Eureka Server 실행:** 서비스 디스커버리를 위해 Eureka Server를 실행합니다.
3.  **Chat Service 실행:**
    ```bash
    # chat-service 디렉토리로 이동하여 아래 명령어 실행
    ./mvnw spring-boot:run
    ```
4.  **API Gateway 실행:** 라우팅을 위해 API Gateway를 실행합니다.
5.  **프론트엔드 실행:** `msa-front` 디렉토리에서 WebSocket을 지원하는 클라이언트를 실행합니다.