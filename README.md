개요
슬랙과 BackOffice 서비스간에 QnA 데이터를 간편하고 빠르게 하기 위해 연계서버를 개발 

시스템 아키텍처
Slack <-> 연계서버 <-> BackOffice

요구사항 정의
1. BackOffice에 등록된 QnA는 자동으로 슬랙의 스레드로 등록이 되여야한다.
Slack <- 연계서버 <- BackOffice (QnA 게시물)
ㄴ BackOffice시스템에서 연계시스템으로 데이터 보내기 API CALL
ㄴ API로 받은 데이터로 Slack에 데이터 전송
ㄴ 필요한 데이터 : 제목, 내용, 작성자, 게시글 식별자, 슬랙 스레드 아이디, BO도메인, 게시글 링크, 부모게시물ID(댓글전용)

2. 슬랙에 지정된 채널에는 지정된 BackOffice의 QnA만 스레드로 자동 등록될수있다.
ex) A채널 -> A BackOffice의 QnA만 스레드로 자동 등록이됨.
    B채널 -> B BackOffice의 QnA만 스레드로 자동 등록이됨.

3. 슬랙에 자동등록된 QnA(스레드)에 댓글을 달면 BackOffice에도 자동으로 댓글이 동기화 되어야된다.
Slack -> 연계서버 -> BackOffice (QnA 게시물 댓글)
ㄴ 연계서버에서 BackOffice의 댓글작성 API CALL
ㄴ 필요한 데이터 : 제목, 내용, 작성자, 게시글 식별자, 슬랙 스레드 아이디, BO도메인, 게시글 링크, 부모게시물ID(댓글전용)

4. 슬랙에 지정된 채널에는 지정된 BackOffice의 댓글 API를 호출할수 있다.
   ex) A채널 -> A BackOffice의 QnA의 댓글 등록만 가능.
       B채널 -> B BackOffice의 QnA의 댓글 등록만 가능.

5. BackOffice에 등록된 QnA의 댓글은 자동으로 슬랙의 스레드 댓글로 등록이 되여야한다.
Slack <- 연계서버 <- BackOffice (QnA 게시물 댓글)
ㄴ BackOffice시스템에서 연계시스템으로 데이터 보내기 API CALL
ㄴ API로 받은 데이터로 Slack에 데이터 전송
ㄴ 필요한 데이터 : 제목, 내용, 작성자, 게시글 식별자, 슬랙 스레드 아이디, BO도메인, 게시글 링크, 부모게시물ID(댓글전용)