package co.acta.slackwebhook.service.modal;

import co.acta.slackwebhook.service.modal.interfaces.SlackModalAPI;
import co.acta.slackwebhook.utils.UtilsModal;
import co.acta.slackwebhook.vo.DomainInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InputReplyDeleteAPI extends UtilsModal implements SlackModalAPI {
    @Override
    public Map<?, ?> makeModalFrame(DomainInfo domainInfo) {
        Map<String, Object> inputBlock = new HashMap<>();
        inputBlock.put("type", "input");
        inputBlock.put("block_id", "reply_delete_block");
        inputBlock.put("label", plainText("답글 삭제 API"));
        Map<String, Object> element = new HashMap<>();
        element.put("type", "plain_text_input");
        element.put("action_id", "input_reply_delete");
        element.put("placeholder", plainText("답글 삭제 API를 입력하세요 (예: https://www.example.com/board/reply/delete)"));
        if (domainInfo != null && domainInfo.getReplyDeleteApi() != null && !domainInfo.getReplyDeleteApi().isEmpty())
            element.put("initial_value", domainInfo.getReplyDeleteApi());
        inputBlock.put("element", element);
        return inputBlock;
    }
}
