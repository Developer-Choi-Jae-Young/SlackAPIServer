package co.acta.slackwebhook.utils;

import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.exception.CustomException;
import co.acta.slackwebhook.exception.ExceptionInfo;
import co.acta.slackwebhook.service.message.SlackMessageLayout;
import co.acta.slackwebhook.service.modal.SlackModalLayout;
import co.acta.slackwebhook.utils.auth.interfaces.Authenticate;
import co.acta.slackwebhook.utils.callback.SlackSendCallBack;
import co.acta.slackwebhook.utils.enums.LoginType;
import co.acta.slackwebhook.vo.BoardDomainInfo;
import co.acta.slackwebhook.vo.SlackEventRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${slack.token}")
    private String slackToken;

    public Map<String, String> filesGetUploadURLExternal(MultipartFile mfile) throws CustomException {
        try {
            String getUrl = "https://slack.com/api/files.getUploadURLExternal"
                    + "?filename=" + URLEncoder.encode(Objects.requireNonNull(mfile.getOriginalFilename()), StandardCharsets.UTF_8.name())
                    + "&length=" + mfile.getSize();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(slackToken);

            ResponseEntity<Map<String, Object>> urlRes = restTemplate.exchange(
                    getUrl, HttpMethod.GET, new HttpEntity<>(headers), getMapType());
            Map<String, Object> urlBody = urlRes.getBody();

            if (urlBody == null || !Boolean.TRUE.equals(urlBody.get("ok"))) {
                log.error("[filesGetUploadURLExternal] Slack API 실패 응답: {}", urlBody);
                throw new CustomException(ExceptionInfo.FILE_UPLOAD_URL_ERROR);
            }

            String uploadUrl = (String) urlBody.get("upload_url");
            String fileId = (String) urlBody.get("file_id");

            HttpHeaders binaryHeaders = new HttpHeaders();
            binaryHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            restTemplate.postForEntity(uploadUrl, new HttpEntity<>(mfile.getBytes(), binaryHeaders), String.class);

            Map<String, String> fileMap = new HashMap<>();
            fileMap.put("id", fileId);
            return fileMap;

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[filesGetUploadURLExternal] 파일 업로드 URL 발급 중 오류: {}", mfile.getOriginalFilename(), e);
            throw new CustomException(ExceptionInfo.FILE_UPLOAD_URL_ERROR);
        }
    }

    public List<Map<String, Object>> filesCompleteUploadExternal(List<Map<String, String>> uploadedFiles, String channelId) throws CustomException {
        try {
            String completeUrl = "https://slack.com/api/files.completeUploadExternal";
            Map<String, Object> completeBody = new HashMap<>();
            completeBody.put("files", uploadedFiles);
            completeBody.put("channel_id", channelId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(slackToken);

            ResponseEntity<Map<String, Object>> completeRes = restTemplate.postForEntity(
                    completeUrl, new HttpEntity<>(completeBody, headers), getMapType());
            Map<String, Object> completeResBody = completeRes.getBody();

            if (completeResBody == null || !Boolean.TRUE.equals(completeResBody.get("ok"))) {
                log.error("[filesCompleteUploadExternal] Slack API 실패 응답: {}", completeResBody);
                throw new CustomException(ExceptionInfo.FILE_UPLOAD_COMPLETE_ERROR);
            }

            return (List<Map<String, Object>>) completeResBody.get("files");

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[filesCompleteUploadExternal] 파일 업로드 완료 처리 중 오류: {}", e.getMessage(), e);
            throw new CustomException(ExceptionInfo.FILE_UPLOAD_COMPLETE_ERROR);
        }
    }

    public HttpHeaders login(String url, String paramId, String paramPw, String id, String pw, LoginType loginType) throws CustomException {
        MultiValueMap<String, String> loginParams = new LinkedMultiValueMap<>();
        loginParams.add(paramId, id);
        loginParams.add(paramPw, pw);

        ResponseEntity<String> loginResponse;
        try {
            loginResponse = restTemplate.postForEntity(url, loginParams, String.class);
        } catch (Exception e) {
            log.error("[login] 로그인 요청 실패: {}", e.getMessage());
            throw new CustomException(ExceptionInfo.LOGIN_FAIL);
        }

        Authenticate strategy = authenticate.stream()
                .filter(auth -> auth.supports(loginType))
                .findFirst()
                .orElseThrow(() -> new CustomException(ExceptionInfo.NOT_SUPPORT_LOGIN_TYPE));

        return strategy.login(loginResponse);
    }

    public ResponseEntity<String> reply(BoardDomainInfo info, String text, String user, HttpHeaders httpHeaders, List<SlackEventRequest.SlackFile> files) throws CustomException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(info.getParamBoardId(), info.getBoardId());
        body.add(info.getParamContent(), text);
        body.add(info.getParamRegUserName(), user);
        body.add(info.getParamRegDttm(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        if (files != null) {
            for (SlackEventRequest.SlackFile fileInfo : files) {
                String downloadUrl = fileInfo.getUrlPrivate();
                String fileName = fileInfo.getName();
                if (downloadUrl != null) body.add("file", makeDownloadFile(downloadUrl, fileName));
            }
        }

        try {
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, httpHeaders);
            return restTemplate.postForEntity(info.getReplyUrl(), requestEntity, String.class);
        } catch (RestClientException e) {
            log.error("[reply] 전송 실패: {}", e.getMessage());
            throw new CustomException(ExceptionInfo.REPLY_FAIL);
        }
    }

    private HttpEntity<?> makeDownloadFile(String downloadUrl, String fileName) throws CustomException {
        Resource fileResource = downloadSlackFile(downloadUrl, fileName);
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentDispositionFormData("file", fileName);
        fileHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new HttpEntity<>(fileResource, fileHeaders);
    }

    private Resource downloadSlackFile(String url, String fileName) throws CustomException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(slackToken);

        ResponseEntity<byte[]> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
        } catch (Exception e) {
            log.error("[downloadSlackFile] 파일 다운로드 실패: {}", fileName);
            throw new CustomException(ExceptionInfo.REPLY_FILE_DOWNLOAD_ERROR);
        }

        byte[] data = Objects.requireNonNull(response.getBody(), "Slack 파일 다운로드 응답이 비어있습니다.");

        return new ByteArrayResource(data) {
            @Override public String getFilename() { return fileName; }
            @Override public long contentLength() { return data.length; }
        };
    }

    public String sendMessage(AddBoardDto boardDto, String channelId, String parentTs, SlackSendCallBack slackSendCallBack) throws CustomException {
        String url = "https://slack.com/api/chat.postMessage";

        List<Map<String, Object>> blocks = slackSendCallBack.callback();
        Map<String, Object> body = slackMessageLayout.makeLayout(boardDto, blocks, channelId, parentTs);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(slackToken);

        try {
            ResponseEntity<Map<String, Object>> res = restTemplate.postForEntity(
                    url, new HttpEntity<>(body, headers), getMapType());
            Map<String, Object> responseBody = res.getBody();

            if (responseBody != null && Boolean.TRUE.equals(responseBody.get("ok"))) {
                return (String) responseBody.get("ts");
            }

            log.error("[sendMessage] Slack API 실패 응답: {}", responseBody);
            throw new CustomException(ExceptionInfo.SLACK_MESSAGE_SEND_FAIL);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[sendMessage] 메시지 전송 중 오류: {}", e.getMessage(), e);
            throw new CustomException(ExceptionInfo.SLACK_MESSAGE_SEND_FAIL);
        }
    }

    public ResponseEntity<Map<String, Object>> openModal(String triggerId, String channelId, SlackSendCallBack slackSendCallBack) throws CustomException {
        String url = "https://slack.com/api/views.open";

        List<Map<String, Object>> blocks = slackSendCallBack.callback();
        Map<String, Object> payload = slackModalLayout.makeLayout(triggerId, channelId, blocks);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(slackToken);

            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                    url, new HttpEntity<>(payload, headers), getMapType());
            log.info("[openModal] Slack API Response: {}", response.getBody());
            return response;
        } catch (Exception e) {
            log.error("[openModal] 모달 오픈 실패: {}", e.getMessage());
            throw new CustomException(ExceptionInfo.MODAL_OPEN_FAIL);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<Map<String, Object>> getMapType() {
        return (Class<Map<String, Object>>) (Class<?>) Map.class;
    }
}
