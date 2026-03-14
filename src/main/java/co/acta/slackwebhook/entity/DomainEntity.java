package co.acta.slackwebhook.entity;

import co.acta.slackwebhook.vo.DomainChannelRequest;
import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DomainEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String domain;
    private String replyUrl;
    private String replyUpdateUrl;
    private String replyDeleteUrl;
    private String viewUrl;
    private String loginUrl;
    private String accountId;
    private String accountPw;
    private String paramNameUserId;
    private String paramNameUserPw;
    private String paramNameBoardId;
    private String paramNameReplyId;   // BO 댓글 PK 파라미터명 (수정/삭제 시 사용)
    private String paramNameReplyIdKey; // BO 댓글 등록 응답 JSON에서 replyId를 꺼낼 key명
    private String paramNameContent;
    private String paramNameRegUsrNm;
    private String paramNameRegDttm;

    public void update(DomainChannelRequest request, String encryptedPassword) {
        this.domain          = request.getHost();
        this.viewUrl         = request.getView();
        this.loginUrl        = request.getLogin();
        this.replyUrl        = request.getReply();
        this.replyUpdateUrl  = request.getReplyUpdate();
        this.replyDeleteUrl  = request.getReplyDelete();
        this.accountId       = request.getReplyId();
        this.accountPw       = encryptedPassword;
        this.paramNameUserId = request.getParamUserId();
        this.paramNameUserPw = request.getParamUserPw();
        this.paramNameBoardId   = request.getParamBoardId();
        this.paramNameReplyId   = request.getParamReplyId();
        this.paramNameReplyIdKey = request.getParamReplyIdKey();
        this.paramNameContent   = request.getParamContent();
        this.paramNameRegUsrNm  = request.getParamRegUser();
        this.paramNameRegDttm   = request.getParamRegDttm();
    }

    public void maskDecryptedPassword(String decryptedPw) {
        this.accountPw = decryptedPw;
    }
}
