package co.acta.slackwebhook.dto.request;

import lombok.Data;

/**
 * BO → 연계서버: 답글 삭제 요청 DTO
 * BO가 답글을 삭제했을 때 Slack 스레드도 동기화하기 위해 호출
 */
@Data
public class BoReplyDeleteDto {
    /** BO 시스템의 댓글 PK */
    private String boReplyId;
}
