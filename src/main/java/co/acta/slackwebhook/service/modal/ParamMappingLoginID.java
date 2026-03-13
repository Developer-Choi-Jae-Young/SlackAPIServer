package co.acta.slackwebhook.service.modal;

import co.acta.slackwebhook.service.modal.interfaces.SlackModalAPI;
import co.acta.slackwebhook.utils.UtilsModal;
import co.acta.slackwebhook.vo.DomainInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ParamMappingLoginID extends UtilsModal implements SlackModalAPI {
    @Override
    public Map<?, ?> makeModalFrame(DomainInfo domainInfo) {
        Map<String, Object> inputBlock = new HashMap<>();
        inputBlock.put("type", "input");
        inputBlock.put("block_id", "param_login_id_block");
        inputBlock.put("label", plainText("파라미터 매핑 - 로그인 ID"));
        Map<String, Object> element = new HashMap<>();
        element.put("type", "plain_text_input");
        element.put("action_id", "input_param_login_id");
        element.put("placeholder", plainText("파라미터 매핑 - 로그인 ID를 입력하세요"));
        if(domainInfo != null && domainInfo.getParamMappingLoginId() != null && !domainInfo.getParamMappingLoginId().isEmpty()) element.put("initial_value", domainInfo.getParamMappingLoginId());
        inputBlock.put("element", element);

        return inputBlock;
    }
}
