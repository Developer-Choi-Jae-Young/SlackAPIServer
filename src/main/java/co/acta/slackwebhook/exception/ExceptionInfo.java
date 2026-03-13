package co.acta.slackwebhook.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExceptionInfo {
    SEND_MESSAGE_ERROR(100, "게시글 알림에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    BASIC_INFO_NOT_FOUNT(101, "기본정보를 찾을수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_SUPPORT_LOGIN_TYPE(102, "지원하지 않는 로그인 타입입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    LOGIN_FAIL(103, "로그인에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    REPLY_FILE_DOWNLOAD_ERROR(104, "[답변] - 파일 업드로 및 다운로드에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    REPLY_FAIL(105, "[답변] - 작성에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    MODAL_OPEN_FAILL(105, "[모달] - 열기에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int errorCode;
    private final String message;
    private final HttpStatus httpStatus;
}
