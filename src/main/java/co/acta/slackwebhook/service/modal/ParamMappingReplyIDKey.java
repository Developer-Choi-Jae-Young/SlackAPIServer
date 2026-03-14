package co.acta.slackwebhook.service.modal;

import co.acta.slackwebhook.service.modal.interfaces.SlackModalAPI;
import co.acta.slackwebhook.utils.UtilsModal;
import co.acta.slackwebhook.vo.DomainInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * BO 댓글 등록 응답 JSON에서 댓글 PK를 꺼낼 key명 설정
 * (예: 응답이 {"replyNo": 123} 이면 "replyNo" 입력)
 * BO가 PK를 응답에 포함하지 않으면 빈칸으로 두면 됨
 */
@Component
public class ParamMappingReplyIDKey extends UtilsModal implements SlackModalAPI {
    @Override
    public Map<?, ?> makeModalFrame(DomainInfo domainInfo) {
        Map<String, Object> inputBlock = new HashMap<>();
        inputBlock.put("type", "input");
        inputBlock.put("block_id", "param_reply_id_key_block");
        inputBlock.put("optional", true);
        inputBlock.put("label", plainText("BO 등록 응답 - 댓글 PK 키명"));
        Map<String, Object> element = new HashMap<>();
        element.put("type", "plain_text_input");
        element.put("action_id", "input_param_reply_id_key");
        element.put("placeholder", plainText("BO 댓글 등록 응답 JSON의 PK 키명 (없으면 빈칸)"));
        if (domainInfo != null && domainInfo.getParamMappingReplyIdKey() != null && !domainInfo.getParamMappingReplyIdKey().isEmpty())
            element.put("initial_value", domainInfo.getParamMappingReplyIdKey());
        inputBlock.put("element", element);

        return inputBlock;
    }
}
