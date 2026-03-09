package co.acta.slackwebhook.service;

import co.acta.slackwebhook.utils.HttpHeader;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlackModal {
    public void openModal(String triggerId) {
        String url = "https://slack.com/api/views.open";

        // 1. View 구성
        Map<String, Object> view = new HashMap<>();
        view.put("type", "modal");
        view.put("callback_id", "user_info_modal");
        view.put("title", plainText("정보 입력창"));
        view.put("submit", plainText("제출하기"));
        view.put("close", plainText("취소"));

        // 2. Blocks 구성 (내용물)
        List<Map<String, Object>> blocks = new ArrayList<>();

        // --- 섹션 블록 (설명글) ---
        Map<String, Object> section = new HashMap<>();
        section.put("type", "section");
        Map<String, Object> sectionText = new HashMap<>();
        sectionText.put("type", "mrkdwn");
        sectionText.put("text", "*안녕하세요!* 아래 정보를 입력해 주세요.");
        section.put("text", sectionText);
        blocks.add(section);

        // --- 인풋 블록 (텍스트 입력) ---
        Map<String, Object> inputBlock = new HashMap<>();
        inputBlock.put("type", "input");
        inputBlock.put("block_id", "name_block");
        inputBlock.put("label", plainText("이름"));

        Map<String, Object> element = new HashMap<>();
        element.put("type", "plain_text_input");
        element.put("action_id", "name_input");
        element.put("placeholder", plainText("이름을 입력하세요 (예: 홍길동)"));

        inputBlock.put("element", element);
        blocks.add(inputBlock);

        view.put("blocks", blocks);

        // 3. 전체 Payload 구성
        Map<String, Object> payload = new HashMap<>();
        payload.put("trigger_id", triggerId);
        payload.put("view", view);

        // 4. 전송 설정 (RestTemplate 사용)
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, HttpHeader.headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            System.out.println("Slack API Response: " + response.getBody());
        } catch (Exception e) {
            System.err.println("모달 오픈 실패: " + e.getMessage());
        }
    }

    /**
     * Java 8에서 중복되는 plain_text 구조 생성을 도와주는 유틸 메서드
     */
    private Map<String, Object> plainText(String text) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "plain_text");
        map.put("text", text);
        return map;
    }
}
