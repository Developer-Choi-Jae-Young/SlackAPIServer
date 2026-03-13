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
    private String viewUrl;
    private String loginUrl;
    private String accountId;
    private String accountPw;
    private String paramNameUserId;
    private String paramNameUserPw;
    private String paramNameBoardId;
    private String paramNameContent;
    private String paramNameRegUsrNm;
    private String paramNameRegDttm;

    public void update(DomainChannelRequest request, String encryptedPassword) {
        this.domain          = request.getHost();
        this.viewUrl         = request.getView();
        this.loginUrl        = request.getLogin();
        this.replyUrl        = request.getReply();
        this.accountId       = request.getReplyId();
        this.accountPw       = encryptedPassword;
        this.paramNameUserId = request.getParamUserId();
        this.paramNameUserPw = request.getParamUserPw();
        this.paramNameBoardId   = request.getParamBoardId();
        this.paramNameContent   = request.getParamContent();
        this.paramNameRegUsrNm  = request.getParamRegUser();
        this.paramNameRegDttm   = request.getParamRegDttm();
    }

    public void maskDecryptedPassword(String decryptedPw) {
        this.accountPw = decryptedPw;
    }
}
