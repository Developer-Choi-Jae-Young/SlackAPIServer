# 💬 Slack WebHook Integration Server (SlackAPIServer)

백오피스(BackOffice) 시스템과 슬랙(Slack) 간의 **QnA/게시글 및 댓글 데이터 양방향 자동 동기화**를 담당하는 연계 서버(Integration Server)입니다.

---

## 📌 1. 프로젝트 개요 (Overview)

본 프로젝트는 백오피스 서비스와 슬랙(Slack) 채널을 연동하여 업무 효율성을 증대시키고, 게시글 작성 및 댓글 소통을 실시간으로 상호 동기화하기 위한 **Spring Boot 기반 중계 API 서버**입니다.

### 💡 핵심 연동 시나리오
1. **[BO ➡️ Slack] 게시글 자동 생성**: 백오피스에 QnA 또는 게시글이 등록되면 설정된 슬랙 채널에 스레드로 자동 포스팅됩니다.
2. **[Slack ➡️ BO] 댓글 양방향 동기화**: 슬랙 스레드에 작성된 댓글은 백오피스 로그인 세션을 통해 해당 게시글의 댓글로 자동 등록됩니다.
3. **[채널별 BO 매핑] 모달 설정 UI**: 슬랙 명령어/이벤트를 통해 특정 슬랙 채널과 백오피스 도메인 및 API 파라미터 매핑을 연동 모달 창(Modal)으로 손쉽게 설정합니다.

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
- **Spring Security Crypto**: AES 기반 `TextEncryptor` (백오피스 계정 비밀번호 암복호화)

### External Integration & Libraries
- **Slack API Integration**: Slack Block Kit (Message & Modal UI), Event Subscriptions, Slack External File Upload API
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

    subgraph IntegrationServer ["SlackAPIServer (Spring Boot)"]
        Ctrl[WebHookCtrl]
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

    BO_API -- "1. Post Board (/slack/add-board)" --> Ctrl
    Ctrl --> Service
    Service -- "2. Send Block Kit Message" --> RestAPI
    RestAPI -- "3. Post Message" --> Slack_UI

    Slack_UI -- "4. Thread Reply Event (/slack/event)" --> Ctrl
    Service -- "5. Decrypt Password & Login" --> Crypto
    Service -- "6. Sync Reply API Call" --> BO_API

    Slack_Modal -- "7. Channel-Domain Config (/slack/interactivity)" --> Ctrl
    Service -- "8. Save Binding Info" --> DB
```

### 3.2 핵심 데이터 흐름 (Data Flow)

#### 1) 백오피스 게시글 ➡️ 슬랙 스레드 동기화
1. 백오피스에서 게시글 등록 시 연계 서버 `/slack/add-board` API 호출
2. 해당 도메인과 매핑된 슬랙 채널 정보(`DomainChannelEntity`) 조회
3. `SlackMessageAPI` 프레임워크를 통해 Slack Block Kit UI 구성을 자동 생성
4. Slack API (`chat.postMessage`)를 호출하여 채널 메시지 전송 및 첨부파일 지원
5. 발급된 슬랙 메시지 타임스탬프(`ts`)와 백오피스 게시글 ID(`boardId`)를 `BoardEntity`에 매핑 및 저장

#### 2) 슬랙 스레드 댓글 ➡️ 백오피스 댓글 동기화
1. 슬랙 스레드 댓글 작성 시 Slack Event Subscriptions가 `/slack/event`로 이벤트 전송
2. `ts` 및 채널 정보로 백오피스 연동 정보(`BoardEntity`, `DomainEntity`) 검색
3. 저장된 백오피스 계정 비밀번호를 복호화(`TextEncryptor`) 후 백오피스 자동 로그인 API 호출 및 세션(Cookie) 획득
4. 백오피스 댓글 작성 API 호출 (슬랙 첨부파일 바이너리 다운로드 후 백오피스 업로드 재전송 지원)

---

## 🏛️ 4. 소프트웨어 공학적 설계 구조: SlackMessageAPI & SlackModalAPI

`SlackMessageAPI` 및 `SlackModalAPI`와 `Enum`(`SlackMessageFrame`, `SlackModalFrame`) 기반 아키텍처는 **객체지향 설계 원칙(SOLID)과 고급 디자인 패턴**을 적용하여 모듈화된 UI 조립 프레임워크입니다.

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

    class SlackMessageFrame {
        <<enum>>
        HEADER(SlackMessageHeader.class)
        DIVIDER_1(SlackMessageDivider.class)
        CONTENT(SlackMessageContent.class)
        -Class~SlackMessageAPI~ serviceClass
    }

    SlackMessageFrame --> SlackMessageAPI : References Class Type

    class WebHookService {
        -List~SlackMessageAPI~ slackMessageAPIList
        +sendAPI(...)
    }

    WebHookService --> SlackMessageFrame : Iterates Order
    WebHookService --> SlackMessageAPI : Executes Strategy
```

### 🧠 4.1 적용된 핵심 디자인 패턴

1. **전략 패턴 (Strategy Pattern)**:
   - `SlackMessageAPI` / `SlackModalAPI` 공통 인터페이스를 기반으로, 각 UI 블록 조각(Header, Content, Link, Input 등)을 독립된 전략 클래스(`SlackMessageHeader`, `InputSubscribeDomain` 등)로 캡슐화하여 구현합니다.
2. **Enum 기반 선언적 레지스트리 & 순서 디스패처 (Declarative Enum Registry)**:
   - `SlackMessageFrame` 및 `SlackModalFrame` Enum이 **"어떤 컴포넌트(`Class<? extends API>`)를 어떤 순서(Order)로 조립할 것인가"**를 선언적으로 관리합니다.
   - `Class<? extends SlackMessageAPI>` 제네릭 타입을 통해 컴파일 타임에 타입 안전성을 보장합니다.
3. **파이프라인 조립 패턴 (Pipeline Assembly Pattern)**:
   - `WebHookService`는 Enum에 정의된 순서대로 파이프라인을 순회하면서, 각 구현체가 생성한 단일 Block Kit JSON 조각(Map)을 하나로 조립(Pipeline Aggregate)합니다.
4. **Spring 다형성 의존성 주입 (Polymorphic Dependency Injection)**:
   - Spring Container가 `List<SlackMessageAPI>` 형태로 모든 구현 빈(Bean)을 자동 주입하며, `UtilsCommon.findMessageBean()`이 Enum에 정의된 Class 타입(`isInstance`)과 매핑되는 빈을 동적으로 탐색합니다.

### 📐 4.2 SOLID 원칙 관점에서의 이점

- **SRP (단일 책임 원칙)**: 각 메시지/모달 구현체는 자신에게 할당된 Slack Block JSON 1개의 조립 책임만 가집니다.
- **OCP (개방-폐쇄 원칙)**: 새로운 UI 요소(버튼, 입력 폼 등)를 추가할 때 기존 코드 수정 없이 인터페이스 구현체 작성 후 Enum에 등록만 하면 확장됩니다.
- **DIP (의존 역전 원칙)**: `WebHookService`는 구체 클래스에 의존하지 않고, 추상 인터페이스(`SlackMessageAPI`, `SlackModalAPI`)에만 의존합니다.
- **유지보수성**: UI 구성 요소의 출력 순서 변경은 Enum 내 상수의 선언 순서만 교체하면 즉시 반영됩니다.

---

## 📂 5. 프로젝트 디렉터리 구조 (Directory Structure)

```
SlackAPIServer/
├── build.gradle                        # Gradle 빌드 및 의존성 설정
├── settings.gradle
├── README.md                           # 프로젝트 설명 문서
└── src/
    ├── main/
    │   ├── java/co/acta/slackwebhook/
    │   │   ├── SlackWebHookApplication.java    # Application Main Class
    │   │   ├── config/
    │   │   │   └── ServerConfig.java           # RestTemplate, TextEncryptor 빈 설정
    │   │   ├── controller/
    │   │   │   ├── WebHookCtrl.java            # Slack & BO 연동 컨트롤러 (엔드포인트)
    │   │   │   └── ErrorController.java        # 전역 예외 처리 (ExceptionHandler)
    │   │   ├── dto/
    │   │   │   └── request/
    │   │   │       ├── AddBoardDto.java        # 게시글 등록 Request DTO
    │   │   │       └── AddWebHookDTO.java      # 모달 호출 Trigger DTO
    │   │   ├── entity/
    │   │   │   ├── BoardEntity.java            # 게시글 - 슬랙 타임스탬프(ts) 매핑 엔티티
    │   │   │   ├── DomainEntity.java           # 백오피스 도메인 및 API 파라미터 엔티티
    │   │   │   └── DomainChannelEntity.java    # 백오피스 도메인 - 슬랙 채널 1:N 매핑 엔티티
    │   │   ├── exception/
    │   │   │   ├── CustomException.java        # 커스텀 예외 클래스
    │   │   │   └── ExceptionInfo.java          # 에러 코드 및 메시지 정의 Enum
    │   │   ├── repository/
    │   │   │   ├── BoardRepository.java
    │   │   │   ├── DomainRepository.java
    │   │   │   └── DomainChannelRepository.java
    │   │   ├── service/
    │   │   │   ├── WebHookService.java         # 메인 비즈니스 로직 연동 서비스
    │   │   │   ├── message/                    # Slack Message Block Kit 생성 모듈
    │   │   │   │   ├── SlackMessageLayout.java
    │   │   │   │   ├── SlackMessageHeader.java
    │   │   │   │   ├── SlackMessageContent.java
    │   │   │   │   ├── SlackMessageLink.java
    │   │   │   │   ├── SlackMessageFile.java
    │   │   │   │   └── interfaces/SlackMessageAPI.java
    │   │   │   └── modal/                      # Slack Modal Block Kit 생성 모듈
    │   │   │       ├── SlackModalLayout.java
    │   │   │       ├── InputSubscribeDomain.java
    │   │   │       ├── InputViewAPI.java
    │   │   │       ├── InputReplyAPI.java
    │   │   │       └── interfaces/SlackModalAPI.java
    │   │   ├── utils/
    │   │   │   ├── CallRestAPI.java            # RestTemplate 기반 Slack & BO REST API 통신
    │   │   │   ├── UtilsCommon.java            # 유틸리티 (Bean 검색, HTTP Host 등)
    │   │   │   ├── auth/                       # 세션/토큰 인증 처리 전략 패턴
    │   │   │   │   ├── SessionAuthenticate.java
    │   │   │   │   └── interfaces/Authenticate.java
    │   │   │   └── enums/                      # 프레임워크 Enum 정의
    │   │   └── vo/                             # 내부/외부 통신 VO 객체
    │   └── resources/
    │       └── application.properties          # 서버 설정 파일 (DB, Port, Crypto, Slack Token)
    └── test/
```

---

## 🗄️ 6. 데이터베이스 엔티티 구조 (Database ERD)

```mermaid
erDiagram
    DOMAIN_ENTITY ||--o{ DOMAIN_CHANNEL : "has many"
    DOMAIN_CHANNEL ||--o{ BOARD_ENTITY : "has many"

    DOMAIN_ENTITY {
        Long id PK
        String domain "백오피스 도메인 주소"
        String viewUrl "게시글 조회 URL"
        String loginUrl "백오피스 로그인 URL"
        String replyUrl "댓글 등록 URL"
        String accountId "연동용 백오피스 계정 ID"
        String accountPw "암호화된 계정 비밀번호"
        String paramNameUserId "로그인 ID 파라미터명"
        String paramNameUserPw "로그인 PW 파라미터명"
        String paramNameBoardId "게시글 ID 파라미터명"
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
        String ts "슬랙 메시지 타임스탬프 (Thread ID)"
    }
```

---

## 🔌 7. API 명세서 (API Specification)

| HTTP Method | Endpoint | 설명 | 주요 요청 데이터 |
| :--- | :--- | :--- | :--- |
| `POST` | `/slack/add-domain-channel` | 슬랙에서 채널 설정 모달(Modal) 오픈 | `trigger_id`, `channel_id` |
| `POST` | `/slack/interactivity` | 슬랙 모달 작성 완료 제출 이벤트 처리 | `payload` (view_submission 데이터) |
| `POST` | `/slack/add-board` | 백오피스 게시글을 슬랙 스레드로 등록 | `dto` (`AddBoardDto`), `files` (MultipartFile) |
| `POST` | `/slack/event` | 슬랙 스레드 댓글 이벤트를 수신하여 BO 동기화 | `SlackEventRequest` JSON, `X-Slack-Retry-Num` |

---

## ⚙️ 8. 환경 설정 및 실행 방법 (Configuration & Setup)

### 8.1 `application.properties` 설정
`src/main/resources/application.properties` 설정 파일에 슬랙 토큰 및 DB 정보를 설정합니다.

```properties
spring.application.name=SlackWebHook
server.port=8888
server.address=0.0.0.0

# MySQL Database
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/slack?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul
spring.datasource.username=root
spring.datasource.password=root

spring.jpa.hibernate.ddl-auto=update
spring.jpa.generate-ddl=true

# 파일 업로드 용량 제한
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# 비밀번호 암호화 Key & Salt
crypto.password=your-secret-key
crypto.salt=ab12cd34ef567890

# Slack Bot User OAuth Token
slack.token=xoxb-your-slack-bot-token
```

### 8.2 애플리케이션 실행
```bash
# Gradle 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```