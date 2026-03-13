package co.acta.slackwebhook.controller;

import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.dto.request.AddWebHookDTO;
import co.acta.slackwebhook.service.WebHookService;
import co.acta.slackwebhook.utils.UtilsCommon;
import co.acta.slackwebhook.vo.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/slack")
public class WebHookCtrl {
    private final WebHookService webHookService;

    @PostMapping(value = "/add-domain-channel")
    public ResponseEntity<?> addDomainChannel(AddWebHookDTO dto) {
        ResponseEntity<Map> response = webHookService.openModal(dto.getTrigger_id(), dto.getChannel_id());

        boolean isOk = (boolean) response.getBody().get("ok");
        log.info("Slack Open PopUp Result: {}", isOk);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/add-board")
    public ResponseEntity<?> addBoard(HttpServletRequest request, @RequestPart("dto") AddBoardDto dto, @RequestPart(value = "files", required = false)List<MultipartFile> files) {
        String finalHost = UtilsCommon.getHost(request);
        List<BoardDomainInfo> boardDomainInfoList = webHookService.sendAPI(dto, finalHost, files);
        return ResponseEntity.ok().body(boardDomainInfoList);
    }

    @PostMapping("/event")
    public ResponseEntity<?> slackCheckEvent(@RequestBody SlackEventRequest request, @RequestHeader(value = "X-Slack-Retry-Num", required = false) Integer retryNum) {
        if (retryNum != null && retryNum > 0) return ResponseEntity.ok().build();
        if (request.getChallenge() != null) return ResponseEntity.ok(request.getChallenge());

        SlackEventRequest.EventDetail event = request.getEvent();
        if (event == null) return ResponseEntity.ok().build();

        ResponseEntity<String> response = null;
        if (event.isUserReplyMessage()) {
            response = webHookService.sendReply(
                    event.getThreadTs(),
                    event.getChannel(),
                    event.getText(),
                    event.getUser(),
                    event.getFiles()
            );
        }

        return ResponseEntity.ok().body(response != null ? response.getBody() : null);
    }

    @PostMapping(value = "/interactivity", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> interactivity(@RequestParam("payload") String payloadString) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        SlackPayload payload = objectMapper.readValue(payloadString, SlackPayload.class);

        DomainInfo domainInfo = null;
        if ("view_submission".equals(payload.getType())) {
            String channelId = payload.getView().getPrivate_metadata();
            DomainChannelRequest request = DomainChannelRequest.of(payload);
            domainInfo = webHookService.addDomainChannel(request, channelId);
        }
        String resultMessage = domainInfo != null ? "저장 완료" : "저장 실패";
        log.info("Add or Update Setting Result : {}", resultMessage);
        return ResponseEntity.ok().build();
    }
}
