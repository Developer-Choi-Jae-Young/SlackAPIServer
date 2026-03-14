package co.acta.slackwebhook.controller;

import co.acta.slackwebhook.dto.request.BoReplyDeleteDto;
import co.acta.slackwebhook.dto.request.BoReplyUpdateDto;
import co.acta.slackwebhook.service.WebHookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/bo/reply")
public class BoReplyCtrl {

    private final WebHookService webHookService;

    /**
     * BO → 연계서버: 답글 수정
     * BO에서 답글 수정 시 호출 → Slack 스레드 메시지 동기화
     */
    @PostMapping("/update")
    public ResponseEntity<?> updateReply(@RequestBody BoReplyUpdateDto dto) {
        log.info("[BO→Slack] 답글 수정 요청. boReplyId={}", dto.getBoReplyId());
        webHookService.boUpdateReplyToSlack(dto.getBoReplyId(), dto.getContent());
        return ResponseEntity.ok().build();
    }

    /**
     * BO → 연계서버: 답글 삭제
     * BO에서 답글 삭제 시 호출 → Slack 스레드 메시지 동기화
     */
    @PostMapping("/delete")
    public ResponseEntity<?> deleteReply(@RequestBody BoReplyDeleteDto dto) {
        log.info("[BO→Slack] 답글 삭제 요청. boReplyId={}", dto.getBoReplyId());
        webHookService.boDeleteReplyToSlack(dto.getBoReplyId());
        return ResponseEntity.ok().build();
    }
}
