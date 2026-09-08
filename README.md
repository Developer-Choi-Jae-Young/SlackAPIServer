# 💬 Slack WebHook Integration Server v2 (SlackAPIServer)

백오피스(BackOffice) 시스템과 슬랙(Slack) 간의 **게시글 및 댓글/답글 양방향 실시간 동기화**와 **보안 검증(HMAC-SHA256)**을 제공하는 연계 서버(Integration Server) v2 버전입니다.

---

## 📌 1. 프로젝트 개요 (Overview)

본 프로젝트는 백오피스 서비스와 슬랙(Slack) 채널을 연동하여 업무 소통 효율을 높이고, 게시글 등록은 물론 **댓글의 생성·수정·삭제 양방향 동기화**와 **슬랙 요청 보안 필터링**을 구현한 **Spring Boot 기반 중계 API 서버**입니다.

### 💡 v2 핵심 연동 시나리오
1. **[BO ➡️ Slack] 게시글 자동 생성**: 백오피스에 QnA/게시글 등록 시 매핑된 슬랙 채널에 스레드로 자동 포스팅합니다.
2. **[Slack ↔ BO] 댓글 생성·수정·삭제 양방향 동기화**:
   - **생성**: 슬랙 스레드 댓글 작성 시 BO 댓글 등록 API를 자동 호출하고 `ReplyEntity`에 ID를 매핑 저장합니다.
   - **수정**: 슬랙 또는 BO에서 댓글 수정 시 상대방 플랫폼의 댓글 내용도 실시간 업데이트합니다.
   - **삭제**: 슬랙 또는 BO에서 댓글 삭제 시 상대방 플랫폼의 댓글도 삭제 처리합니다.
3. **[보안 강화] Slack HMAC-SHA256 서명 검증**:
   - `SlackSignatureVerificationFilter` 및 `CachedBodyHttpServletRequest`를 통해 슬랙에서 수신되는 요청의 서명(`X-Slack-Signature`)을 검증하여 무단 요청을 차단합니다.
4. **[채널별 BO/서브도메인 매핑] 모달 UI 고도화**:
   - 슬랙 모달 창을 통해 백오피스 도메인, 서브도메인, 댓글 수정/삭제 API URL 및 파라미터 매핑을 유연하게 설정할 수 있습니다.

---

## 🛠️ 2. 기술 스택 (Tech Stack)

### Backend Framework & Language
- **Java**: 1.8 (Java 8)
- **Spring Boot**: 2.7.18
- **Build Tool**: Gradle

### Persistence & Data Access
- **Database**: MySQL
- **ORM / Data**: Spring Data JPA, Hibernate, Spring Data JDBC
- **Database Driver**: `com.mysql:mysql-connector-j`

### Security & Encryption
- **Request Verification**: HMAC-SHA256 기반 `SlackSignatureVerificationFilter` & `CachedBodyHttpServletRequest`
- **Password Encryption**: Spring Security Crypto AES 기반 `TextEncryptor` (BO 계정 비밀번호 암복호화)

### External Integration & Libraries
- **Slack API Integration**: Slack Block Kit (Message & Modal UI), Event Subscriptions, Slack API (`chat.postMessage`, `chat.update`, `chat.delete`), Slack File Upload API
- **HTTP Client**: Spring `RestTemplate`
- **Utilities & Automation**: Lombok, Jackson (`ObjectMapper`)

---

## 🏗️ 3. 시스템 아키텍처 및 데이터 흐름 (Architecture & Flow)

### 3.1 전체 시스템 아키텍처

```mermaid
flowchart LR
    subgraph BackOffice ["BackOffice System"]
        BO_DB[(BackOffice DB)]
        BO_API[BO REST API]
    end

    subgraph IntegrationServer ["SlackAPIServer v2 (Spring Boot)"]
        Filter[SlackSignatureFilter]
        WebHookCtrl[WebHookCtrl]
        BOCtrl[BackOfficeCtrl]
        Service[WebHookService]
        RestAPI[CallRestAPI]
        Crypto[TextEncryptor]
        DB[(MySQL DB)]
    end

    subgraph Slack ["Slack Platform"]
        Slack_UI[Slack Channel / Thread]
        Slack_Modal[Slack Interactivity Modal]
        Slack_API[Slack REST API / Events]
    end

    BO_API -- "1. Post Board (/bo/board/add)" --> BOCtrl
    BO_API -- "2. Sync Reply Update/Delete (/bo/reply/*)" --> BOCtrl
    
    Slack_API -- "3. Incoming Events/Modals" --> Filter
    Filter -- "4. Signature Verified Request" --> WebHookCtrl

    WebHookCtrl --> Service
    BOCtrl --> Service

    Service -- "5. Decrypt Password & Sync" --> Crypto
    Service -- "6. Call BO REST API" --> RestAPI
    RestAPI -- "7. Response / Sync" --> BO_API

    Service -- "8. Call Slack REST API (post/update/delete)" --> RestAPI
    RestAPI -- "9. Slack UI Render" --> Slack_UI

    Service -- "10. Store Mapping (Board & Reply)" --> DB
```

### 3.2 핵심 데이터 흐름 (Data Flow)

#### 1) 백오피스 게시글 ➡️ 슬랙 스레드 포스팅
1. 백오피스에서 게시글 등록 시 `/bo/board/add` API 호출
2. 요청 도메인과 매핑된 슬랙 채널(`DomainChannelEntity`) 탐색
3. `SlackMessageAPI` 파이프라인으로 Slack Block Kit UI 생성 및 파일 첨부 지원
4. Slack API (`chat.postMessage`) 호출 후 메시지 타임스탬프(`ts`)를 `BoardEntity`에 저장

#### 2) 슬랙 스레드 댓글 ➡️ 백오피스 댓글 동기화 & 저장
1. 슬랙 스레드에 댓글 작성 시 Slack Event Subscriptions가 `/slack/event`로 이벤트 전송
2. `SlackSignatureVerificationFilter`에서 `X-Slack-Signature` 검증
3. `ts` 및 채널 정보로 `BoardEntity` 및 `DomainEntity` 조회 후 BO 로그인 및 댓글 작성 API 호출
4. 전달받은 BO 댓글 식별자(`boReplyId`)와 슬랙 댓글 타임스탬프(`replyTs`)를 `ReplyEntity`에 저장

#### 3) 댓글 수정 / 삭제 실시간 양방향 동기화 (v2 신규)
- **Slack ➡️ BO**: 슬랙 댓글 수정/삭제 이벤트 발생 시 `ReplyEntity`에서 `boReplyId`를 조회하여 BO의 댓글 수정/삭제 API 호출
- **BO ➡️ Slack**: BO에서 댓글 수정/삭제 시 `/bo/reply/update`, `/bo/reply/delete` 호출 ➡️ `ReplyEntity`에서 `replyTs` 조회 후 Slack API (`chat.update`, `chat.delete`) 호출

#### 4) Slack HMAC-SHA256 요청 서명 검증 필터 (v2 신규)
1. 슬랙에서 도래하는 모든 `/slack/*` HTTP 요청 수신
2. `CachedBodyHttpServletRequest`로 래핑하여 HTTP Request Body가 컨트롤러 도달 전 유실되지 않도록 래핑
3. `v0:timestamp:body` 조합 및 HMAC-SHA256 비밀키(`slack.signing-secret`)로 서명 검증

---

## 🏛️ 4. 소프트웨어 공학적 설계 구조: SlackMessageAPI & SlackModalAPI

`SlackMessageAPI` 및 `SlackModalAPI`는 **SOLID 원칙과 전략 패턴(Strategy Pattern)**을 기반으로 모듈화되어, UI 구조 변경 및 새로운 입력 항목 추가 시 기존 코드 변경을 최소화합니다.

```mermaid
classDiagram
    class SlackMessageAPI {
        <<interface>>
        +makeMessageFrame(boardDto, files, channelId) Map
    }
    class SlackMessageHeader {
        +makeMessageFrame(...) Map
    }
    class SlackMessageContent {
        +makeMessageFrame(...) Map
    }
    class SlackMessageDivider {
        +makeMessageFrame(...) Map
    }

    SlackMessageAPI <|.. SlackMessageHeader
    SlackMessageAPI <|.. SlackMessageContent
    SlackMessageAPI <|.. SlackMessageDivider

    class SlackModalAPI {
        <<interface>>
        +makeModalFrame() Map
    }
    class InputSubscribeDomain {
        +makeModalFrame() Map
    }
    class InputSubscribeSubDomain {
        +makeModalFrame() Map
    }
    class InputReplyUpdateAPI {
        +makeModalFrame() Map
    }
    class InputReplyDeleteAPI {
        +makeModalFrame() Map
    }

    SlackModalAPI <|.. InputSubscribeDomain
    SlackModalAPI <|.. InputSubscribeSubDomain
    SlackModalAPI <|.. InputReplyUpdateAPI
    SlackModalAPI <|.. InputReplyDeleteAPI
```

### 📐 4.1 SOLID 원칙 적용점
- **SRP (단일 책임 원칙)**: 각 Block Kit 컴포넌트는 단일 UI 블록 구성만 책임집니다.
- **OCP (개방-폐쇄 원칙)**: 서브도메인, 댓글 수정/삭제 API 설정 등 v2에 새로 추가된 UI 구성 요소를 기존 코드 수정 없이 구현체 클래스 추가 및 Enum 등록으로 확장했습니다.
- **DIP (의존 역전 원칙)**: `WebHookService`는 구체 클래스가 아닌 `SlackMessageAPI` / `SlackModalAPI` 추상 인터페이스에 의존합니다.

---

## 📂 5. 프로젝트 디렉터리 구조 (Directory Structure)

```
SlackAPIServer/
├── build.gradle                        # Gradle 빌드 및 의존성 설정
├── settings.gradle
├── README.md                           # 프로젝트 설명 문서 (v2)
└── src/
    ├── main/
    │   ├── java/co/acta/slackwebhook/
    │   │   ├── SlackWebHookApplication.java    # Spring Boot 메인 클래스
    │   │   ├── config/
    │   │   │   └── ServerConfig.java           # RestTemplate, TextEncryptor 빈 설정
    │   │   ├── controller/
    │   │   │   ├── BackOfficeCtrl.java         # BO 요청 처리 컨트롤러 (게시글/댓글 연동)
    │   │   │   ├── WebHookCtrl.java            # Slack WebHook & Event 수신 컨트롤러
    │   │   │   └── ErrorController.java        # 전역 예외 처리 컨트롤러
    │   │   ├── dto/request/
    │   │   │   ├── AddBoardDto.java            # 게시글 등록 요청 DTO
    │   │   │   ├── AddWebHookDTO.java          # 모달 오픈 요청 DTO
    │   │   │   ├── BoReplyDeleteDto.java       # BO 댓글 삭제 요청 DTO
    │   │   │   └── BoReplyUpdateDto.java       # BO 댓글 수정 요청 DTO
    │   │   ├── entity/
    │   │   │   ├── BoardEntity.java            # BO 게시글 - Slack 타임스탬프(ts) 매핑
    │   │   │   ├── DomainEntity.java           # BO 도메인/서브도메인 & API 파라미터 매핑
    │   │   │   ├── DomainChannelEntity.java    # BO 도메인 - Slack 채널 1:N 매핑
    │   │   │   └── ReplyEntity.java            # BO 댓글 ID - Slack 댓글 타임스탬프(replyTs) 매핑
    │   │   ├── exception/
    │   │   │   ├── CustomException.java        # 커스텀 예외 클래스
    │   │   │   └── ExceptionInfo.java          # 예외 메시지 & 코드 Enum
    │   │   ├── filter/
    │   │   │   ├── CachedBodyHttpServletRequest.java       # Stream 재사용 요청 래퍼
    │   │   │   └── SlackSignatureVerificationFilter.java   # Slack HMAC-SHA256 서명 검증 필터
    │   │   ├── repository/
    │   │   │   ├── BoardRepository.java
    │   │   │   ├── DomainChannelRepository.java
    │   │   │   ├── DomainRepository.java
    │   │   │   └── ReplyRepository.java
    │   │   ├── service/
    │   │   │   ├── WebHookService.java         # 메인 연동 비즈니스 로직 서비스
    │   │   │   ├── message/                    # Slack Message Block Kit 렌더링 모듈
    │   │   │   │   ├── SlackMessageLayout.java
    │   │   │   │   ├── SlackMessageHeader.java
    │   │   │   │   ├── SlackMessageContent.java
    │   │   │   │   ├── SlackMessageDivider.java
    │   │   │   │   ├── SlackMessageDivider2.java
    │   │   │   │   ├── SlackMessageFile.java
    │   │   │   │   └── interfaces/SlackMessageAPI.java
    │   │   │   └── modal/                      # Slack Modal Block Kit 렌더링 모듈
    │   │   │       ├── SlackModalLayout.java
    │   │   │       ├── InputSubscribeDomain.java
    │   │   │       ├── InputSubscribeSubDomain.java
    │   │   │       ├── InputReplyUpdateAPI.java
    │   │   │       ├── InputReplyDeleteAPI.java
    │   │   │       └── interfaces/SlackModalAPI.java
    │   │   ├── utils/
    │   │   │   ├── CallRestAPI.java            # Slack REST API 및 BO API 통신 유틸리티
    │   │   │   ├── UtilsCommon.java            # 공통 유틸리티
    │   │   │   ├── auth/                       # BO 로그인 인증 전략 패턴
    │   │   │   └── enums/                      # Slack UI 프레임 Enum 정의
    │   │   └── vo/                             # 내부/외부 전달 VO 객체
    │   └── resources/
    │       └── application.properties          # 서버 설정 파일 (DB, Port, Slack Signing Secret 등)
    └── test/
```

---

## 🗄️ 6. 데이터베이스 엔티티 구조 (Database ERD)

```mermaid
erDiagram
    DOMAIN_ENTITY ||--o{ DOMAIN_CHANNEL : "has many"
    DOMAIN_CHANNEL ||--o{ BOARD_ENTITY : "has many"
    BOARD_ENTITY ||--o{ REPLY_ENTITY : "has many"

    DOMAIN_ENTITY {
        Long id PK
        String domain "백오피스 도메인 주소"
        String subDomain "백오피스 서브도메인 주소"
        String viewUrl "게시글 조회 URL"
        String loginUrl "백오피스 로그인 URL"
        String replyUrl "댓글 작성 URL"
        String replyUpdateUrl "댓글 수정 URL"
        String replyDeleteUrl "댓글 삭제 URL"
        String accountId "연동용 백오피스 계정 ID"
        String accountPw "암호화된 계정 비밀번호"
        String paramNameUserId "로그인 ID 파라미터명"
        String paramNameUserPw "로그인 PW 파라미터명"
        String paramNameBoardId "게시글 ID 파라미터명"
        String paramNameReplyId "댓글 ID 파라미터명"
        String paramNameReplyIdKey "댓글 ID Key 파라미터명"
        String paramNameContent "내용 파라미터명"
        String paramNameRegUsrNm "작성자 파라미터명"
        String paramNameRegDttm "작성일시 파라미터명"
    }

    DOMAIN_CHANNEL {
        Long id PK
        Long domainId FK "DomainEntity 참조"
        String channel "슬랙 채널 ID"
    }

    BOARD_ENTITY {
        Long id PK
        Long domainChannelId FK "DomainChannelEntity 참조"
        Long boardId "백오피스 게시글 식별자"
        String title "게시글 제목"
        String content "게시글 내용"
        String writer "작성자"
        LocalDate regDate "작성일자"
        String ts "슬랙 메인 메시지 타임스탬프 (Thread ID)"
    }

    REPLY_ENTITY {
        Long id PK
        Long boardId FK "BoardEntity 참조"
        String ts "스레드 메인 타임스탬프"
        String replyTs UK "슬랙 댓글 타임스탬프"
        String boReplyId "백오피스 댓글 식별자"
    }
```

---

## 🔌 7. API 명세서 (API Specification)

| 구분 | HTTP Method | Endpoint | 설명 | 주요 요청 데이터 |
| :--- | :--- | :--- | :--- | :--- |
| **Slack** | `POST` | `/slack/add-domain-channel` | 슬랙 채널 설정 모달(Modal) 오픈 | `trigger_id`, `channel_id` |
| **Slack** | `POST` | `/slack/interactivity` | 슬랙 모달 작성 제출 이벤트 처리 (도메인/채널 매핑 저장) | `payload` (`view_submission`) |
| **Slack** | `POST` | `/slack/event` | 슬랙 스레드 댓글 생성/수정/삭제 이벤트 수신 ➡️ BO 동기화 | `SlackEventRequest`, `X-Slack-Signature` |
| **BO** | `POST` | `/bo/board/add` | 백오피스 게시글 등록 ➡️ 슬랙 스레드 생성 | `dto` (`AddBoardDto`), `files` (MultipartFile) |
| **BO** | `POST` | `/bo/reply/update` | 백오피스 댓글 수정 ➡️ 슬랙 스레드 댓글 수정 | `BoReplyUpdateDto` (`boReplyId`, `content`) |
| **BO** | `POST` | `/bo/reply/delete` | 백오피스 댓글 삭제 ➡️ 슬랙 스레드 댓글 삭제 | `BoReplyDeleteDto` (`boReplyId`) |

---

## ⚙️ 8. 환경 설정 및 실행 방법 (Configuration & Setup)

### 8.1 `application.properties` 설정
`src/main/resources/application.properties` 설정 파일에 슬랙 토큰, 서명 검증 키, DB 접속 정보를 설정합니다.

```properties
spring.application.name=SlackWebHook
server.address=0.0.0.0
server.port=8888

# Database Config (MySQL)
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/slack?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.generate-ddl=true

# 파일 업로드 용량 제한
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# 비밀번호 암호화 Key & Salt
crypto.password=${CRYPTO_PASSWORD:this-is-our-secret-link-key}
crypto.salt=${CRYPTO_SALT:ab12cd34ef567890}

# Slack Integration Config
slack.token=${SLACK_TOKEN:xoxb-your-slack-bot-token}
slack.signing-secret=${SLACK_SIGNING_SECRET:your-slack-signing-secret}
```

### 8.2 애플리케이션 빌드 및 실행
```bash
# Gradle 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```