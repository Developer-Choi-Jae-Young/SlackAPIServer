package co.acta.slackwebhook.service.modal;

import co.acta.slackwebhook.service.modal.interfaces.SlackModalAPI;
import co.acta.slackwebhook.utils.UtilsModal;
import co.acta.slackwebhook.vo.DomainInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ParamMappingLoginPW extends UtilsModal implements SlackModalAPI {
    @Override
    public Map<?, ?> makeModalFrame(DomainInfo domainInfo) {
        Map<String, Object> inputBlock = new HashMap<>();
        inputBlock.put("type", "input");
        inputBlock.put("block_id", "param_login_pw_block");
        inputBlock.put("label", plainText("파라미터 매핑 - 로그인 PW"));
        Map<String, Object> element = new HashMap<>();
        element.put("type", "plain_text_input");
        element.put("action_id", "input_param_login_pw");
        element.put("placeholder", plainText("파라미터 매핑 - 로그인 PW를 입력하세요"));
        if(domainInfo != null && domainInfo.getParamMappingLoginPw() != null && !domainInfo.getParamMappingLoginPw().isEmpty()) element.put("initial_value", domainInfo.getParamMappingLoginPw());
        inputBlock.put("element", element);
        
        return inputBlock;
    }
}
