package co.acta.slackwebhook.dto.request;

import lombok.Data;

/**
 * BO → 연계서버: 답글 수정 요청 DTO
 * BO가 답글을 수정했을 때 Slack 스레드도 동기화하기 위해 호출
 */
@Data
public class BoReplyUpdateDto {
    /** BO 시스템의 댓글 PK */
    private String boReplyId;
    /** 수정된 댓글 내용 */
    private String content;
}
