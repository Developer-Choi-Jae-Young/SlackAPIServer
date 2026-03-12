package co.acta.slackwebhook.service.modal;

import co.acta.slackwebhook.utils.UtilsModal;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SlackModalLayout extends UtilsModal {
    public Map<String, Object> makeLayout(String triggerId, String channelId, List<Map<String, Object>> blocks) {
        Map<String, Object> view = new HashMap<>();
        Map<String, Object> payload = new HashMap<>();
        view.put("type", "modal");
        view.put("callback_id", "domain_info_modal");
        view.put("private_metadata", channelId);
        view.put("title", plainText("기본 정보 입력"));
        view.put("submit", plainText("제출하기"));
        view.put("close", plainText("취소"));
        view.put("blocks", blocks);
        payload.put("trigger_id", triggerId);
        payload.put("view", view);

        return payload;
    }
}
