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
        webHookService.openModal(dto.getTrigger_id(), dto.getChannel_id());
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
            String ts = (String) event.get("thread_ts");
            String channel = (String) event.get("channel");
            List<Map<String, Object>> files = (List<Map<String, Object>>) event.get("files");

            if (event.get("bot_id") == null && "message".equals(type)) {
                System.out.println("사용자(" + user + ")의 메시지: " + text);
                webHookService.sendReply(ts, channel, text, user, files);
            }
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/interactivity", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> interactivity(@RequestParam("payload") String payloadString) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> payload = mapper.readValue(payloadString, new TypeReference<Map<String, Object>>() {});

        String type = (String) payload.get("type");

        if ("view_submission".equals(type)) {
            Map<String, Object> view = (Map<String, Object>) payload.get("view");
            String channelId = (String) view.get("private_metadata");

            Map<String, Object> state = (Map<String, Object>) view.get("state");
            Map<String, Object> values = (Map<String, Object>) state.get("values");

            String hostValue = getValue(values, "host_block", "input_host");
            String viewValue = getValue(values, "view_block", "input_view");
            String replyValue = getValue(values, "reply_block", "input_reply");
            String replyPwValue = getValue(values, "reply_pw_block", "input_reply_pw");
            String replyIdValue = getValue(values, "reply_id_block", "input_reply_id");
            String loginValue = getValue(values, "login_block", "input_login");


            String paramUserId = getValue(values, "param_login_id_block", "input_param_login_id");
            String paramUserPw = getValue(values, "param_login_pw_block", "input_param_login_pw");
            String paramBoardId = getValue(values, "param_reply_board_id_block", "input_param_reply_board_id");
            String paramContent = getValue(values, "param_reply_board_content_block", "input_param_reply_board_content");
            String paramRegUser = getValue(values, "param_reply_board_writer_block", "input_param_reply_board_writer");
            String paramRegDttm = getValue(values, "param_reply_board_reg_date_block", "input_param_reply_board_reg_date");

            webHookService.addDomainChannel(hostValue, viewValue, loginValue, replyValue, channelId, replyIdValue, replyPwValue, paramUserId, paramUserPw, paramBoardId, paramContent, paramRegUser, paramRegDttm);
        }

        return ResponseEntity.ok().build();
    }

    private String getValue(Map<String, Object> values, String blockId, String actionId) {
        Map<String, Object> block = (Map<String, Object>) values.get(blockId);
        if (block != null) {
            Map<String, Object> action = (Map<String, Object>) block.get(actionId);
            if (action != null) {
                return (String) action.get("value");
            }
        }
        return null;
    }
}
