package co.acta.slackwebhook.Utils;

import co.acta.slackwebhook.service.*;
import co.acta.slackwebhook.service.interfaces.SlackSendAPI;

public enum SlackMessageFrame {
    HEADER(SlackMessageHeader.class),
    DIVIDER_1(SlackMessageDivider.class),
    INFO(SlackMessageInfo.class),
    CONTENT_HEADER(SlackMessageContentHeader.class),
    DIVIDER_2(SlackMessageDivider.class), // 중복 사용 가능
    CONTENT(SlackMessageContent.class),
    FILE(SlackMessageFile.class),
    LINK(SlackMessageLink.class);

    private final Class<? extends SlackSendAPI> serviceClass;

    SlackMessageFrame(Class<? extends SlackSendAPI> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public Class<? extends SlackSendAPI> getServiceClass() {
        return serviceClass;
    }
}
