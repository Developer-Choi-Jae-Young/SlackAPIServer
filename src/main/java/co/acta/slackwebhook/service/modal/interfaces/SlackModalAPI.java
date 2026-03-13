package co.acta.slackwebhook.service.modal.interfaces;

import co.acta.slackwebhook.vo.DomainInfo;

import java.util.Map;

public interface SlackModalAPI {
    Map<?, ?> makeModalFrame(DomainInfo domainInfo);
}
