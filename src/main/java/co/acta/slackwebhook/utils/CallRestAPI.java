package co.acta.slackwebhook.utils;

import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.service.message.SlackMessageLayout;
import co.acta.slackwebhook.service.modal.SlackModalLayout;
import co.acta.slackwebhook.utils.auth.interfaces.Authenticate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallRestAPI {
    private final RestTemplate restTemplate;
    private final SlackMessageLayout slackMessageLayout;
    private final SlackModalLayout slackModalLayout;
    private final List<Authenticate> authenticate;

    public Map<String, String> filesGetUploadURLExternal(MultipartFile mfile) {
        Map<String, String> fileMap = new HashMap<>();

        try {
            String getUrl = "https://slack.com/api/files.getUploadURLExternal"
                    + "?filename=" + URLEncoder.encode(Objects.requireNonNull(mfile.getOriginalFilename()), StandardCharsets.UTF_8.name())
                    + "&length=" + mfile.getSize();

            ResponseEntity<Map> urlRes = restTemplate.exchange(getUrl, HttpMethod.GET, new HttpEntity<>(HttpHeader.headers), Map.class);
            Map urlBody = urlRes.getBody();

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
        Map completeResBody = completeRes.getBody();

        if (completeResBody != null && (boolean) completeResBody.get("ok")) {
            filesInfo = (List<Map<String, Object>>) completeResBody.get("files");
        }

        return filesInfo;
    }

    public HttpHeaders login(String url, String paramId, String paramPw, String id, String pw, LoginType loginType) {
        MultiValueMap<String, String> loginParams = new LinkedMultiValueMap<>();
        loginParams.add(paramId, id);
        loginParams.add(paramPw, pw);
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(url, loginParams, String.class);

        Authenticate strategy = authenticate.stream().filter(auth -> auth.supports(loginType)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 로그인 타입입니다."));
        return strategy.login(loginResponse);
    }

    public void reply(String url, String paramBoardId, String paramBoardContent, String paramBoardRegUserName, String paramBoardRegDttm,
                      Long boardId, String text, String user, HttpHeaders httpHeaders, List<Map<String, Object>> files) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(paramBoardId, boardId);
        body.add(paramBoardContent, text);
        body.add(paramBoardRegUserName, user);
        body.add(paramBoardRegDttm, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        if (files != null && !files.isEmpty()) {
            for (Map<String, Object> fileInfo : files) {
                String downloadUrl = (String) fileInfo.get("url_private_download");
                String fileName = (String) fileInfo.get("name");
                if (downloadUrl != null) body.add("file", makeDownloadFile(downloadUrl, fileName));
            }
        }

        try {
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, httpHeaders);
            restTemplate.postForEntity(url, requestEntity, String.class);
        } catch (RestClientException e) {
            log.error("전송 실패: {}", e.getMessage());
        }
    }

    private HttpEntity<?> makeDownloadFile(String downloadUrl, String fileName) {
        Resource fileResource = downloadSlackFile(downloadUrl, fileName);
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentDispositionFormData("file", fileName);
        fileHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new HttpEntity<>(fileResource, fileHeaders);
    }

    private Resource downloadSlackFile(String url, String fileName) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<?> entity = new HttpEntity<>(HttpHeader.headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
        byte[] data = response.getBody();

        return new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return fileName;
            }

            @Override
            public long contentLength() {
                return data.length;
            }
        };
    }

    public String sendMessage(AddBoardDto boardDto, String channelId, String parentTs, SlackSendCallBack slackSendCallBack) {
        String messageTs = "";
        String url = "https://slack.com/api/chat.postMessage";

        List<Map<String, Object>> blocks = slackSendCallBack.callback();
        Map<String, Object> body = slackMessageLayout.makeLayout(boardDto, blocks, channelId, parentTs);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, HttpHeader.headers);
        ResponseEntity<Map> res = restTemplate.postForEntity(url, request, Map.class);

        Map responseBody = res.getBody();
        if (responseBody != null && Boolean.TRUE.equals(responseBody.get("ok"))) {
            messageTs = (String) responseBody.get("ts");
        } else {
            log.error("슬랙 메시지 전송 실패: {}", responseBody);
        }

        return messageTs;
    }

    public void openModal(String triggerId, String channelId, SlackSendCallBack slackSendCallBack) {
        String url = "https://slack.com/api/views.open";

        List<Map<String, Object>> blocks = slackSendCallBack.callback();
        Map<String, Object> payload = slackModalLayout.makeLayout(triggerId, channelId, blocks);
        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, HttpHeader.headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            log.info("Slack API Response: {}", response.getBody());
        } catch (Exception e) {
            log.info("모달 오픈 실패: {}", e.getMessage());
        }
    }
}
