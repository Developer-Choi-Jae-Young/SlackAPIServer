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
        view.put("title", plainText("도메인 정보 입력"));
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
