package co.acta.slackwebhook.service.modal;

import co.acta.slackwebhook.service.modal.interfaces.SlackModalAPI;
import co.acta.slackwebhook.utils.UtilsModal;
import co.acta.slackwebhook.vo.DomainInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * BO 수정/삭제 API 호출 시 댓글 PK를 전달할 파라미터명 설정
 * (예: BO 수정 API가 replyId 라는 파라미터로 댓글을 특정한다면 "replyId" 입력)
 * BO가 PK 파라미터를 요구하지 않으면 빈칸으로 두면 됨
 */
@Component
public class ParamMappingReplyID extends UtilsModal implements SlackModalAPI {
    @Override
    public Map<?, ?> makeModalFrame(DomainInfo domainInfo) {
        Map<String, Object> inputBlock = new HashMap<>();
        inputBlock.put("type", "input");
        inputBlock.put("block_id", "param_reply_id_block");
        inputBlock.put("optional", true);
        inputBlock.put("label", plainText("파라미터 매핑 - 댓글 번호 (수정/삭제용)"));
        Map<String, Object> element = new HashMap<>();
        element.put("type", "plain_text_input");
        element.put("action_id", "input_param_reply_id");
        element.put("placeholder", plainText("BO 수정/삭제 API의 댓글 PK 파라미터명 (없으면 빈칸)"));
        if (domainInfo != null && domainInfo.getParamMappingReplyId() != null && !domainInfo.getParamMappingReplyId().isEmpty())
            element.put("initial_value", domainInfo.getParamMappingReplyId());
        inputBlock.put("element", element);

        return inputBlock;
    }
}
