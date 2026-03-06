package co.acta.slackwebhook.controller;

import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.dto.request.AddWebHookDTO;
import co.acta.slackwebhook.service.WebHookService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/slack")
public class WebHookCtrl {
    private final WebHookService webHookService;

    @PostMapping(value = "/add-domain-channel")
    public ResponseEntity<?> addDomainChannel(AddWebHookDTO dto) {
        webHookService.addDomainChannel(dto.getText(), dto.getChannel_id());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/add-board")
    public ResponseEntity<?> addBoard(HttpServletRequest request, @RequestPart("dto") AddBoardDto dto, @RequestPart(value = "files", required = false)List<MultipartFile> files) {
        String rawOrigin = request.getHeader("Origin");
        if (rawOrigin == null || rawOrigin.isEmpty()) {
            rawOrigin = request.getHeader("Referer");
        }
        if (rawOrigin == null || rawOrigin.isEmpty()) {
            rawOrigin = request.getHeader("Host");
        }

        String finalHost = "";
        try {
            if (rawOrigin.startsWith("http")) {
                java.net.URL url = new URL(rawOrigin);
                finalHost = url.getHost();
                if (url.getPort() != -1) {
                    finalHost += ":" + url.getPort();
                }
            } else {
                finalHost = rawOrigin;
            }
        } catch (Exception e) {
            finalHost = rawOrigin.replace("http://", "").replace("https://", "").split("/")[0];
        }

        webHookService.sendAPI(dto, finalHost, files);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/event")
    public ResponseEntity<?> slackCheckEvent(@RequestBody Map<String, Object> payload) {
        Map<String, Object> event = (Map<String, Object>) payload.get("event");
        if (payload.containsKey("challenge")) return ResponseEntity.ok(payload.get("challenge"));

        if (event != null) {
            String type = (String) event.get("type");
            String text = (String) event.get("text");
            String user = (String) event.get("user");

            if (event.get("bot_id") == null && "message".equals(type)) {
                System.out.println("사용자(" + user + ")의 메시지: " + text);

                // 여기서 DB 저장이나 비즈니스 로직을 실행하세요!
            }
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/interactivity", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> interactivity(@RequestParam("payload") String payloadString) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> payload = mapper.readValue(payloadString, new TypeReference<Map<String, Object>>() {});

        String type = (String) payload.get("type");

        if ("block_actions".equals(type)) {
            List<Map<String, Object>> actions = (List<Map<String, Object>>) payload.get("actions");
            if (!actions.isEmpty()) {
                Map<String, Object> action = actions.get(0);
                String actionId = (String) action.get("action_id");
                String value = (String) action.get("value");

                // 사용자 정보
                Map<String, Object> userMap = (Map<String, Object>) payload.get("user");
                String userId = (String) userMap.get("id");

                System.out.println("버튼 클릭 주체: " + userId);
                System.out.println("클릭된 버튼 값: " + value);

                // 여기서 "승인" 또는 "거절" 로직을 처리하세요!
            }
        }

        return ResponseEntity.ok().build();
    }
}
