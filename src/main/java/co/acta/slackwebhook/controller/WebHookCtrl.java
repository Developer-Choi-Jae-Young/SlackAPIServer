package co.acta.slackwebhook.controller;

import co.acta.slackwebhook.dto.request.AddWebHookDTO;
import co.acta.slackwebhook.service.WebHookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.Response;
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

    @PostMapping(value = "/add-member")
    public ResponseEntity<?> addMember(AddWebHookDTO dto) {
        webHookService.addMember(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/add-token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> addToken(AddWebHookDTO dto) {
        webHookService.addToken(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/add-board")
    public ResponseEntity<?> addBoard(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String remoteHost = request.getRemoteHost();
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        String referer = request.getHeader("Referer");
        String origin = request.getHeader("Origin");

        webHookService.sendAPI("web hook test", origin);
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
