package co.acta.slackwebhook.vo;

import co.acta.slackwebhook.entity.DomainEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainInfo {
    private String domain;
    private String viewApi;
    private String replyApi;
    private String loginApi;
    private String loginId;
    private String loginPw;
    private String paramMappingLoginId;
    private String paramMappingLoginPw;
    private String paramMappingBoardId;
    private String paramMappingBoardContent;
    private String paramMappingBoardWriter;
    private String paramMappingBoardRegDttm;

    public static DomainInfo of(DomainEntity domainEntity) {
        return domainEntity == null ? null : DomainInfo.builder()
                .domain(domainEntity.getDomain())
                .viewApi(domainEntity.getViewUrl())
                .replyApi(domainEntity.getReplyUrl())
                .loginApi(domainEntity.getLoginUrl())
                .loginId(domainEntity.getAccountId())
                .loginPw(domainEntity.getAccountPw())
                .paramMappingLoginId(domainEntity.getParamNameUserId())
                .paramMappingLoginPw(domainEntity.getParamNameUserPw())
                .paramMappingBoardId(domainEntity.getParamNameBoardId())
                .paramMappingBoardContent(domainEntity.getParamNameContent())
                .paramMappingBoardWriter(domainEntity.getParamNameRegUsrNm())
                .paramMappingBoardRegDttm(domainEntity.getParamNameRegDttm())
                .build();
    }
}
