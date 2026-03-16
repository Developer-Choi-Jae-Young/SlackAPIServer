package co.acta.slackwebhook.controller;

import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.dto.request.BoReplyDeleteDto;
import co.acta.slackwebhook.dto.request.BoReplyUpdateDto;
import co.acta.slackwebhook.service.WebHookService;
import co.acta.slackwebhook.utils.UtilsCommon;
import co.acta.slackwebhook.vo.BoardDomainInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/bo")
public class BackOfficeCtrl {

    private final WebHookService webHookService;

    @PostMapping("/reply/update")
    public ResponseEntity<?> updateReply(@RequestBody BoReplyUpdateDto dto) {
        log.info("[BO→Slack] 답글 수정 요청. boReplyId={}", dto.getBoReplyId());
        webHookService.boUpdateReplyToSlack(dto.getBoReplyId(), dto.getContent());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reply/delete")
    public ResponseEntity<?> deleteReply(@RequestBody BoReplyDeleteDto dto) {
        log.info("[BO→Slack] 답글 삭제 요청. boReplyId={}", dto.getBoReplyId());
        webHookService.boDeleteReplyToSlack(dto.getBoReplyId());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/board/add")
    public ResponseEntity<?> addBoard(HttpServletRequest request, @RequestPart("dto") AddBoardDto dto, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        String finalHost = UtilsCommon.getHost(request);
        List<BoardDomainInfo> boardDomainInfoList = webHookService.sendAPI(dto, finalHost, files);
        return ResponseEntity.ok().body(boardDomainInfoList);
    }
}
