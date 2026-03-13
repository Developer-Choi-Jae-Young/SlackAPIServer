package co.acta.slackwebhook.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@Getter
@Setter
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
}
