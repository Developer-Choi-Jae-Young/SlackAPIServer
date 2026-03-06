package co.acta.slackwebhook.Utils;

import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.service.SlackMessageLayout;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallRestAPI {
    private final RestTemplate restTemplate;
    private final SlackMessageLayout slackMessageLayout;

    public Map<String, String> filesGetUploadURLExternal(MultipartFile mfile) {
        Map<String, String> fileMap = new HashMap<>();

        try {
            String getUrl = "https://slack.com/api/files.getUploadURLExternal"
                    + "?filename=" + URLEncoder.encode(mfile.getOriginalFilename(), "UTF-8")
                    + "&length=" + mfile.getSize();

            ResponseEntity<Map> urlRes = restTemplate.exchange(getUrl, HttpMethod.GET, new HttpEntity<>(HttpHeader.headers), Map.class);
            Map<String, Object> urlBody = urlRes.getBody();

            if (urlBody != null && (boolean) urlBody.get("ok")) {
                String uploadUrl = (String) urlBody.get("upload_url");
                String fileId = (String) urlBody.get("file_id");

                HttpHeaders binaryHeaders = new HttpHeaders();
                binaryHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

                HttpEntity<byte[]> binaryRequest = new HttpEntity<>(mfile.getBytes(), binaryHeaders);
                restTemplate.postForEntity(uploadUrl, binaryRequest, String.class);
                fileMap.put("id", fileId);
            }
        } catch (Exception e) {
            log.error("파일 전송 중 오류 발생: " + mfile.getOriginalFilename(), e);
        }

        return fileMap;
    }

    public List<Map<String, Object>> filesCompleteUploadExternal(List<Map<String, String>> uploadedFiles, String channelId) {
        List<Map<String, Object>> filesInfo = new ArrayList<>();

        String completeUrl = "https://slack.com/api/files.completeUploadExternal";
        Map<String, Object> completeBody = new HashMap<>();
        completeBody.put("files", uploadedFiles);
        completeBody.put("channel_id", channelId);

        ResponseEntity<Map> completeRes = restTemplate.postForEntity(completeUrl, new HttpEntity<>(completeBody, HttpHeader.headers), Map.class);
        Map<String, Object> completeResBody = completeRes.getBody();

        if (completeResBody != null && (boolean) completeResBody.get("ok")) {
            filesInfo = (List<Map<String, Object>>) completeResBody.get("files");
        }

        return filesInfo;
    }

    public void sendMessage(AddBoardDto boardDto, String channelId, SlackSendCallBack slackSendCallBack) {
        String url = "https://slack.com/api/chat.postMessage";

        List<Map<String, Object>> blocks = slackSendCallBack.callback();
        Map<String, Object> body = slackMessageLayout.makeLayout(boardDto, blocks, channelId);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, HttpHeader.headers);
        ResponseEntity<Map> res = restTemplate.postForEntity(url, request, Map.class);
        log.info(res.getBody().toString());
    }
}
