package co.acta.slackwebhook.service.modal;

import co.acta.slackwebhook.service.modal.interfaces.SlackModalAPI;
import co.acta.slackwebhook.utils.UtilsModal;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SlackModalTitle extends UtilsModal implements SlackModalAPI {
    @Override
    public Map<?, ?> makeModalFrame() {
        Map<String, Object> section = new HashMap<>();
        section.put("type", "section");
        Map<String, Object> sectionText = new HashMap<>();
        sectionText.put("type", "mrkdwn");
        sectionText.put("text", "아래 정보를 입력해 주세요.");
        section.put("text", sectionText);

        return section;
    }
}
