package co.acta.slackwebhook.controller;

import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.dto.request.AddWebHookDTO;
import co.acta.slackwebhook.exception.CustomException;
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
    public ResponseEntity<?> addDomainChannel(AddWebHookDTO dto) throws CustomException {
        ResponseEntity<Map<String, Object>> response = webHookService.openModal(dto.getTrigger_id(), dto.getChannel_id());
        boolean isOk = response.getBody() != null && Boolean.TRUE.equals(response.getBody().get("ok"));
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

        if (event.isUserReplyMessage()) {
            webHookService.sendReply(
                    event.getThreadTs(),
                    event.getTs(),       // Slack 답글 자체 ts → replyTs
                    event.getChannel(),
                    event.getText(),
                    event.getUser(),
                    event.getFiles()
            );
        } else if (event.isUserReplyEdited()) {
            // 답글 수정 — Slack은 이미 수정됨, BO에만 반영
            webHookService.updateReply(
                    event.getChannel(),
                    event.getMessage().getTs(),
                    event.getMessage().getText(),
                    event.getMessage().getUser(),
                    event.getMessage().getFiles()
            );
        } else if (event.isUserReplyDeleted()) {
            webHookService.deleteReply(
                    event.getChannel(),
                    event.getDeletedTs()
            );
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/interactivity", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> interactivity(@RequestParam("payload") String payloadString) throws CustomException {
        SlackPayload payload;
        try {
            payload = new ObjectMapper().readValue(payloadString, SlackPayload.class);
        } catch (Exception e) {
            log.error("[interactivity] payload 파싱 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("잘못된 payload 형식입니다.");
        }

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
