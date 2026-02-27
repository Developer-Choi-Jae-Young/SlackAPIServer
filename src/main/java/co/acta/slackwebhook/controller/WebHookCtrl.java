package co.acta.slackwebhook.controller;

import co.acta.slackwebhook.dto.request.AddWebHookDTO;
import co.acta.slackwebhook.service.WebHookService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/slack")
public class WebHookCtrl {
    private final WebHookService webHookService;

    @PostMapping("/add-member")
    public ResponseEntity<?> addWebHook(@RequestBody AddWebHookDTO dto) throws JsonProcessingException {
        webHookService.addMember(dto);
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
}
