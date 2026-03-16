package co.acta.slackwebhook.vo;

import co.acta.slackwebhook.entity.BoardEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardDomainInfo {
    private String replyUrl;
    private String replyUpdateUrl;
    private String replyDeleteUrl;
    private String loginUrl;
    private String accountId;
    private String accountPw;
    private String paramUserId;
    private String paramUserPw;
    private String paramBoardId;
    private String paramReplyId;
    private String paramReplyIdKey;
    private String paramContent;
    private String paramRegUserName;
    private String paramRegDttm;
    private Long boardId;

    public static BoardDomainInfo of(BoardEntity board) {
        return (board == null || board.getDomainChannel() == null || board.getDomainChannel().getDomain() == null) ? null
                : BoardDomainInfo.builder()
                .replyUrl(board.getDomainChannel().getDomain().getReplyUrl())
                .replyUpdateUrl(board.getDomainChannel().getDomain().getReplyUpdateUrl())
                .replyDeleteUrl(board.getDomainChannel().getDomain().getReplyDeleteUrl())
                .loginUrl(board.getDomainChannel().getDomain().getLoginUrl())
                .accountId(board.getDomainChannel().getDomain().getAccountId())
                .accountPw(board.getDomainChannel().getDomain().getAccountPw())
                .paramUserId(board.getDomainChannel().getDomain().getParamNameUserId())
                .paramUserPw(board.getDomainChannel().getDomain().getParamNameUserPw())
                .paramBoardId(board.getDomainChannel().getDomain().getParamNameBoardId())
                .paramReplyId(board.getDomainChannel().getDomain().getParamNameReplyId())
                .paramReplyIdKey(board.getDomainChannel().getDomain().getParamNameReplyIdKey())
                .paramContent(board.getDomainChannel().getDomain().getParamNameContent())
                .paramRegUserName(board.getDomainChannel().getDomain().getParamNameRegUsrNm())
                .paramRegDttm(board.getDomainChannel().getDomain().getParamNameRegDttm())
                .boardId(board.getBoardId())
                .build();
    }
}
