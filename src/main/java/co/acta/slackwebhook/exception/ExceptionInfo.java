package co.acta.slackwebhook.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExceptionInfo {
    SEND_MESSAGE_ERROR(100, "게시글 알림에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    SLACK_MESSAGE_SEND_FAIL(111, "[슬랙] - 메시지 전송에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    BASIC_INFO_NOT_FOUND(101, "기본정보를 찾을수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_SUPPORT_LOGIN_TYPE(102, "지원하지 않는 로그인 타입입니다.", HttpStatus.BAD_REQUEST),
    LOGIN_FAIL(103, "로그인에 실패하였습니다.", HttpStatus.UNAUTHORIZED),
    FILE_UPLOAD_URL_ERROR(104, "[파일] - 업로드 URL 발급에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_UPLOAD_COMPLETE_ERROR(108, "[파일] - 업로드 완료 처리에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    REPLY_FILE_DOWNLOAD_ERROR(109, "[답변] - 파일 업로드 및 다운로드에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    REPLY_FAIL(105, "[답변] - 작성에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    MODAL_OPEN_FAIL(106, "[모달] - 열기에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_SLACK_SIGNATURE(107, "유효하지 않은 Slack 요청입니다.", HttpStatus.UNAUTHORIZED),
    DOMAIN_CHANNEL_SAVE_ERROR(110, "도메인 채널 저장에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    SLACK_MESSAGE_UPDATE_FAIL(112, "[슬랙] - 메시지 수정에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    SLACK_MESSAGE_DELETE_FAIL(113, "[슬랙] - 메시지 삭제에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int errorCode;
    private final String message;
    private final HttpStatus httpStatus;
}
