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
    private String replyUpdateApi;
    private String replyDeleteApi;
    private String loginApi;
    private String loginId;
    private String loginPw;
    private String paramMappingLoginId;
    private String paramMappingLoginPw;
    private String paramMappingBoardId;
    private String paramMappingBoardContent;
    private String paramMappingBoardWriter;
    private String paramMappingBoardRegDttm;
    private String paramMappingReplyId;      // BO 수정/삭제 API의 댓글 PK 파라미터명
    private String paramMappingReplyIdKey;   // BO 등록 응답 JSON에서 댓글 PK를 꺼낼 key명

    public static DomainInfo of(DomainEntity domainEntity) {
        return domainEntity == null ? null : DomainInfo.builder()
                .domain(domainEntity.getDomain())
                .viewApi(domainEntity.getViewUrl())
                .replyApi(domainEntity.getReplyUrl())
                .replyUpdateApi(domainEntity.getReplyUpdateUrl())
                .replyDeleteApi(domainEntity.getReplyDeleteUrl())
                .loginApi(domainEntity.getLoginUrl())
                .loginId(domainEntity.getAccountId())
                .loginPw(domainEntity.getAccountPw())
                .paramMappingLoginId(domainEntity.getParamNameUserId())
                .paramMappingLoginPw(domainEntity.getParamNameUserPw())
                .paramMappingBoardId(domainEntity.getParamNameBoardId())
                .paramMappingBoardContent(domainEntity.getParamNameContent())
                .paramMappingBoardWriter(domainEntity.getParamNameRegUsrNm())
                .paramMappingBoardRegDttm(domainEntity.getParamNameRegDttm())
                .paramMappingReplyId(domainEntity.getParamNameReplyId())
                .paramMappingReplyIdKey(domainEntity.getParamNameReplyIdKey())
                .build();
    }
}
