package co.acta.slackwebhook.utils;

import co.acta.slackwebhook.service.message.interfaces.SlackMessageAPI;
import co.acta.slackwebhook.service.message.*;
import lombok.Getter;

@Getter
public enum SlackMessageFrame {
    HEADER(SlackMessageHeader.class),
    DIVIDER_1(SlackMessageDivider.class),
    INFO(SlackMessageInfo.class),
    CONTENT_HEADER(SlackMessageContentHeader.class),
    DIVIDER_2(SlackMessageDivider.class), // 중복 사용 가능
    CONTENT(SlackMessageContent.class),
    FILE(SlackMessageFile.class),
    LINK(SlackMessageLink.class);

    private final Class<? extends SlackMessageAPI> serviceClass;

    SlackMessageFrame(Class<? extends SlackMessageAPI> serviceClass) {
        this.serviceClass = serviceClass;
    }
}
