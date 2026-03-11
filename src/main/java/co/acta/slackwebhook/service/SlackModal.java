package co.acta.slackwebhook.service;

import co.acta.slackwebhook.utils.HttpHeader;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SlackModal {
    public void openModal(String triggerId, String channelId) {
        String url = "https://slack.com/api/views.open";

        Map<String, Object> view = new HashMap<>();
        view.put("type", "modal");
        view.put("callback_id", "domain_info_modal");
        view.put("private_metadata", channelId);
        view.put("title", plainText("기본 정보 입력"));
        view.put("submit", plainText("제출하기"));
        view.put("close", plainText("취소"));

        List<Map<String, Object>> blocks = new ArrayList<>();

        Map<String, Object> section = new HashMap<>();
        section.put("type", "section");
        Map<String, Object> sectionText = new HashMap<>();
        sectionText.put("type", "mrkdwn");
        sectionText.put("text", "아래 정보를 입력해 주세요.");
        section.put("text", sectionText);
        blocks.add(section);

        Map<String, Object> inputBlock1 = new HashMap<>();
        inputBlock1.put("type", "input");
        inputBlock1.put("block_id", "host_block");
        inputBlock1.put("label", plainText("도메인 주소"));
        Map<String, Object> element1 = new HashMap<>();
        element1.put("type", "plain_text_input");
        element1.put("action_id", "input_host");
        element1.put("placeholder", plainText("도메인 주소를 입력하세요 (예: https://www.example.com)"));
        inputBlock1.put("element", element1);
        blocks.add(inputBlock1);

        Map<String, Object> inputBlock2 = new HashMap<>();
        inputBlock2.put("type", "input");
        inputBlock2.put("block_id", "view_block");
        inputBlock2.put("label", plainText("상세보기 API"));
        Map<String, Object> element2 = new HashMap<>();
        element2.put("type", "plain_text_input");
        element2.put("action_id", "input_view");
        element2.put("placeholder", plainText("상세보기 API를 입력하세요 (예: https://www.example.com/board/view&id=)"));
        inputBlock2.put("element", element2);
        blocks.add(inputBlock2);

        Map<String, Object> inputBlock3 = new HashMap<>();
        inputBlock3.put("type", "input");
        inputBlock3.put("block_id", "reply_block");
        inputBlock3.put("label", plainText("답글 작성 API"));
        Map<String, Object> element3 = new HashMap<>();
        element3.put("type", "plain_text_input");
        element3.put("action_id", "input_reply");
        element3.put("placeholder", plainText("답글 작성 API를 입력하세요 (예: https://www.example.com/board/reply)"));
        inputBlock3.put("element", element3);
        blocks.add(inputBlock3);

        Map<String, Object> inputBlock4 = new HashMap<>();
        inputBlock4.put("type", "input");
        inputBlock4.put("block_id", "reply_id_block");
        inputBlock4.put("label", plainText("답글 작성 계정 ID"));
        Map<String, Object> element4 = new HashMap<>();
        element4.put("type", "plain_text_input");
        element4.put("action_id", "input_reply_id");
        element4.put("placeholder", plainText("답글 작성 계정 PW를 입력하세요"));
        inputBlock4.put("element", element4);
        blocks.add(inputBlock4);

        Map<String, Object> inputBlock5 = new HashMap<>();
        inputBlock5.put("type", "input");
        inputBlock5.put("block_id", "reply_pw_block");
        inputBlock5.put("label", plainText("답글 작성 계정 PW"));
        Map<String, Object> element5 = new HashMap<>();
        element5.put("type", "plain_text_input");
        element5.put("action_id", "input_reply_pw");
        element5.put("placeholder", plainText("답글 작성 계정 PW를 입력하세요"));
        inputBlock5.put("element", element5);
        blocks.add(inputBlock5);

        Map<String, Object> inputBlock6 = new HashMap<>();
        inputBlock6.put("type", "input");
        inputBlock6.put("block_id", "param_login_id_block");
        inputBlock6.put("label", plainText("파라미터 매핑 - 로그인 ID"));
        Map<String, Object> element6 = new HashMap<>();
        element6.put("type", "plain_text_input");
        element6.put("action_id", "input_param_login_id");
        element6.put("placeholder", plainText("파라미터 매핑 - 로그인 ID를 입력하세요"));
        inputBlock6.put("element", element6);
        blocks.add(inputBlock6);

        Map<String, Object> inputBlock7 = new HashMap<>();
        inputBlock7.put("type", "input");
        inputBlock7.put("block_id", "param_login_pw_block");
        inputBlock7.put("label", plainText("파라미터 매핑 - 로그인 PW"));
        Map<String, Object> element7 = new HashMap<>();
        element7.put("type", "plain_text_input");
        element7.put("action_id", "input_param_login_pw");
        element7.put("placeholder", plainText("파라미터 매핑 - 로그인 PW를 입력하세요"));
        inputBlock7.put("element", element7);
        blocks.add(inputBlock7);

        Map<String, Object> inputBlock8 = new HashMap<>();
        inputBlock8.put("type", "input");
        inputBlock8.put("block_id", "param_reply_board_id_block");
        inputBlock8.put("label", plainText("파라미터 매핑 - 게시글 번호"));
        Map<String, Object> element8 = new HashMap<>();
        element8.put("type", "plain_text_input");
        element8.put("action_id", "input_param_reply_board_id");
        element8.put("placeholder", plainText("파라미터 매핑 - 게시글 번호를 입력하세요"));
        inputBlock8.put("element", element8);
        blocks.add(inputBlock8);

        Map<String, Object> inputBlock9 = new HashMap<>();
        inputBlock9.put("type", "input");
        inputBlock9.put("block_id", "param_reply_board_content_block");
        inputBlock9.put("label", plainText("파라미터 매핑 - 게시글 내용"));
        Map<String, Object> element9 = new HashMap<>();
        element9.put("type", "plain_text_input");
        element9.put("action_id", "input_param_reply_board_content");
        element9.put("placeholder", plainText("파라미터 매핑 - 게시글 내용을 입력하세요"));
        inputBlock9.put("element", element9);
        blocks.add(inputBlock9);

        Map<String, Object> inputBlock10 = new HashMap<>();
        inputBlock10.put("type", "input");
        inputBlock10.put("block_id", "param_reply_board_writer_block");
        inputBlock10.put("label", plainText("파라미터 매핑 - 게시글 글쓴이"));
        Map<String, Object> element10 = new HashMap<>();
        element10.put("type", "plain_text_input");
        element10.put("action_id", "input_param_reply_board_writer");
        element10.put("placeholder", plainText("파라미터 매핑 - 게시글 글쓴이를 입력하세요"));
        inputBlock10.put("element", element10);
        blocks.add(inputBlock10);

        Map<String, Object> inputBlock11 = new HashMap<>();
        inputBlock11.put("type", "input");
        inputBlock11.put("block_id", "param_reply_board_reg_date_block");
        inputBlock11.put("label", plainText("파라미터 매핑 - 게시글 등록일"));
        Map<String, Object> element11 = new HashMap<>();
        element11.put("type", "plain_text_input");
        element11.put("action_id", "input_param_reply_board_reg_date");
        element11.put("placeholder", plainText("파라미터 매핑 - 게시글 등록일을 입력하세요"));
        inputBlock11.put("element", element11);
        blocks.add(inputBlock11);

        view.put("blocks", blocks);

        Map<String, Object> payload = new HashMap<>();
        payload.put("trigger_id", triggerId);
        payload.put("view", view);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, HttpHeader.headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            System.out.println("Slack API Response: " + response.getBody());
        } catch (Exception e) {
            System.err.println("모달 오픈 실패: " + e.getMessage());
        }
    }

    private Map<String, Object> plainText(String text) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "plain_text");
        map.put("text", text);
        return map;
    }
}
