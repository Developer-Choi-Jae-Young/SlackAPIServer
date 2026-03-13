package co.acta.slackwebhook.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DomainChannelRequest {
    private String host;
    private String view;
    private String login;
    private String reply;
    private String replyId;
    private String replyPw;
    private String paramUserId;
    private String paramUserPw;
    private String paramBoardId;
    private String paramContent;
    private String paramRegUser;
    private String paramRegDttm;

    public static DomainChannelRequest of(SlackPayload payload) {
        return DomainChannelRequest.builder()
                .host(payload.getActionValue("host_block", "input_host"))
                .view(payload.getActionValue("view_block", "input_view"))
                .login(payload.getActionValue("login_block", "input_login"))
                .reply(payload.getActionValue("reply_block", "input_reply"))
                .replyId(payload.getActionValue("reply_id_block", "input_reply_id"))
                .replyPw(payload.getActionValue("reply_pw_block", "input_reply_pw"))
                .paramUserId(payload.getActionValue("param_login_id_block", "input_param_login_id"))
                .paramUserPw(payload.getActionValue("param_login_pw_block", "input_param_login_pw"))
                .paramBoardId(payload.getActionValue("param_reply_board_id_block", "input_param_reply_board_id"))
                .paramContent(payload.getActionValue("param_reply_board_content_block", "input_param_reply_board_content"))
                .paramRegUser(payload.getActionValue("param_reply_board_writer_block", "input_param_reply_board_writer"))
                .paramRegDttm(payload.getActionValue("param_reply_board_reg_date_block", "input_param_reply_board_reg_date"))
                .build();
    }
}
