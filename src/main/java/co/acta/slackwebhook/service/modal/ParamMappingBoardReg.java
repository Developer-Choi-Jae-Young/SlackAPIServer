package co.acta.slackwebhook.service.modal;

import co.acta.slackwebhook.service.modal.interfaces.SlackModalAPI;
import co.acta.slackwebhook.utils.UtilsModal;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class ParamMappingBoardReg extends UtilsModal implements SlackModalAPI {
    @Override
    public Map<?, ?> makeModalFrame() {
        Map<String, Object> inputBlock = new HashMap<>();
        inputBlock.put("type", "input");
        inputBlock.put("block_id", "param_reply_board_reg_date_block");
        inputBlock.put("label", plainText("파라미터 매핑 - 게시글 등록일"));
        Map<String, Object> element = new HashMap<>();
        element.put("type", "plain_text_input");
        element.put("action_id", "input_param_reply_board_reg_date");
        element.put("placeholder", plainText("파라미터 매핑 - 게시글 등록일을 입력하세요"));
        inputBlock.put("element", element);

        return inputBlock;
    }
}
