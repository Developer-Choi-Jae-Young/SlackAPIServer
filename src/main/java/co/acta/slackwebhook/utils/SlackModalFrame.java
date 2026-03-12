package co.acta.slackwebhook.utils;

import co.acta.slackwebhook.service.message.interfaces.SlackMessageAPI;
import co.acta.slackwebhook.service.message.*;
import co.acta.slackwebhook.service.modal.*;
import co.acta.slackwebhook.service.modal.interfaces.SlackModalAPI;
import lombok.Getter;

@Getter
public enum SlackModalFrame {
    TITLE(SlackModalTitle.class),
    SUBSCRIBE_DOMAIN(InputSubscribeDomain.class),
    VIEW_API(InputViewAPI.class),
    REPLY_API(InputReplyAPI.class),
    LOGIN_API(InputLoginAPI.class),
    ACCOUNT_ID(InputReplyAccountID.class),
    ACCOUNT_PW(InputReplyAccountPW.class),
    MAPPING_LOGIN_ID(ParamMappingLoginID.class),
    MAPPING_LOGIN_PW(ParamMappingLoginPW.class),
    MAPPING_BOARD_ID(ParamMappingBoardID.class),
    MAPPING_BOARD_CONTENT(ParamMappingBoardContent.class),
    MAPPING_BOARD_WRITER(ParamMappingBoardWriter.class),
    MAPPING_BOARD_REG(ParamMappingBoardReg.class),;

    private final Class<? extends SlackModalAPI> serviceClass;

    SlackModalFrame(Class<? extends SlackModalAPI> serviceClass) {
        this.serviceClass = serviceClass;
    }
}
