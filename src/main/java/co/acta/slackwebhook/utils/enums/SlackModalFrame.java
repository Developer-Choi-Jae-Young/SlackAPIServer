package co.acta.slackwebhook.utils.enums;

import co.acta.slackwebhook.service.modal.*;
import co.acta.slackwebhook.service.modal.interfaces.SlackModalAPI;
import lombok.Getter;

@Getter
public enum SlackModalFrame {
    TITLE(SlackModalTitle.class),
    SUBSCRIBE_DOMAIN(InputSubscribeDomain.class),
    VIEW_API(InputViewAPI.class),
    REPLY_API(InputReplyAPI.class),
    REPLY_UPDATE_API(InputReplyUpdateAPI.class),
    REPLY_DELETE_API(InputReplyDeleteAPI.class),
    LOGIN_API(InputLoginAPI.class),
    ACCOUNT_ID(InputReplyAccountID.class),
    ACCOUNT_PW(InputReplyAccountPW.class),
    MAPPING_LOGIN_ID(ParamMappingLoginID.class),
    MAPPING_LOGIN_PW(ParamMappingLoginPW.class),
    MAPPING_BOARD_ID(ParamMappingBoardID.class),
    MAPPING_BOARD_CONTENT(ParamMappingBoardContent.class),
    MAPPING_BOARD_WRITER(ParamMappingBoardWriter.class),
    MAPPING_BOARD_REG(ParamMappingBoardReg.class),
    MAPPING_REPLY_ID(ParamMappingReplyID.class),         // BO 수정/삭제 API의 댓글 PK 파라미터명
    MAPPING_REPLY_ID_KEY(ParamMappingReplyIDKey.class);  // BO 등록 응답에서 댓글 PK 꺼낼 key명

    private final Class<? extends SlackModalAPI> serviceClass;

    SlackModalFrame(Class<? extends SlackModalAPI> serviceClass) {
        this.serviceClass = serviceClass;
    }
}
