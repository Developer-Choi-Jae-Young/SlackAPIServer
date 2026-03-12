package co.acta.slackwebhook.service.modal;

import co.acta.slackwebhook.service.modal.interfaces.SlackModalAPI;
import co.acta.slackwebhook.utils.UtilsModal;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class InputSubscribeDomain extends UtilsModal implements SlackModalAPI {
    @Override
    public Map<?, ?> makeModalFrame() {
        Map<String, Object> inputBlock = new HashMap<>();
        inputBlock.put("type", "input");
        inputBlock.put("block_id", "host_block");
        inputBlock.put("label", plainText("도메인 주소"));
        Map<String, Object> element = new HashMap<>();
        element.put("type", "plain_text_input");
        element.put("action_id", "input_host");
        element.put("placeholder", plainText("도메인 주소를 입력하세요 (예: https://www.example.com)"));
        inputBlock.put("element", element);

        return inputBlock;
    }
}
