package co.acta.slackwebhook.service;

import co.acta.slackwebhook.entity.DomainEntity;
import co.acta.slackwebhook.repository.DomainRepository;
import co.acta.slackwebhook.utils.CallRestAPI;
import co.acta.slackwebhook.utils.HttpHeader;
import co.acta.slackwebhook.utils.SlackMessageFrame;
import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.entity.BoardEntity;
import co.acta.slackwebhook.entity.DomainChannelEntity;
import co.acta.slackwebhook.repository.BoardRepository;
import co.acta.slackwebhook.repository.DomainChannelRepository;
import co.acta.slackwebhook.service.interfaces.SlackSendAPI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebHookService {
    private final DomainChannelRepository domainChannelRepository;
    private final DomainRepository domainRepository;
    private final BoardRepository boardRepository;
    private final List<SlackSendAPI> slackSendAPIList;
    private final CallRestAPI callRestAPI;
    private final TextEncryptor textEncryptor;

    @Transactional
    public void sendAPI(AddBoardDto boardDto, String domain, List<MultipartFile> files) {
        String parentTs = boardDto.getParentBoardId() != null ? boardRepository.findByBoardId((long) boardDto.getParentBoardId()).map(BoardEntity::getTs).orElse(null) : null;
        List<DomainChannelEntity> domainChannelList = domainChannelRepository.findByDomain_Domain(domain);

        domainChannelList.forEach((data) -> {
            boardDto.setLink(data.getDomain().getViewUrl());
            String ts = callRestAPI.sendMessage(boardDto, data.getChannel(), parentTs, () -> {
                List<Map<String, Object>> blocks = new ArrayList<>();

                for (SlackMessageFrame frame : SlackMessageFrame.values()) {
                    findBean(frame.getServiceClass()).ifPresent(service -> {
                        Map<String, Object> result = (Map<String, Object>) service.makeMessageFrame(boardDto, files, data.getChannel());
                        if (result != null && !result.isEmpty()) blocks.add(result);
                    });
                }

                return blocks;
            });

            boardDto.setTs(ts);
            BoardEntity board = addBoard(boardDto, data);
        });
    }

    private Optional<SlackSendAPI> findBean(Class<? extends SlackSendAPI> clazz) {
        return slackSendAPIList.stream().filter(clazz::isInstance).findFirst();
    }

    @Transactional
    public void addDomainChannel(String domain, String viewUrl, String replyUrl, String channel, String replyIdValue, String replyPwValue, String paramUserId, String paramUserPw,String paramBoardId, String paramContent, String paramRegUser, String paramRegDttm) {
        String encPassword = textEncryptor.encrypt(replyPwValue);
        DomainEntity duplicateEntity = domainRepository.findByDomain(domain).orElse(null);

        if(duplicateEntity != null){
            throw new RuntimeException("도메인이 이미 존재합니다.");
        }

        DomainEntity domainEntity = DomainEntity.builder()
                .domain(domain)
                .viewUrl(viewUrl)
                .replyUrl(replyUrl)
                .accountId(replyIdValue)
                .accountPw(encPassword)
                .paramNameUserId(paramUserId)
                .paramNameUserPw(paramUserPw)
                .paramNameBoardId(paramBoardId)
                .paramNameContent(paramContent)
                .paramNameRegUsrNm(paramRegUser)
                .paramNameRegDttm(paramRegDttm)
                .build();

        DomainChannelEntity domainChannel = DomainChannelEntity.builder()
                .domain(domainEntity)
                .channel(channel)
                .build();

        DomainEntity savedDomain = domainRepository.save(domainEntity);

        if(!domainChannelRepository.findByDomainAndChannel(savedDomain, channel).isEmpty()) throw new RuntimeException("이미 등록된 채널");
        
        domainChannelRepository.save(domainChannel);
    }

    @Transactional
    public BoardEntity addBoard(AddBoardDto addBoardDto, DomainChannelEntity domainChannel) {
        BoardEntity board = BoardEntity.builder()
                .title(addBoardDto.getTitle())
                .content(addBoardDto.getContent())
                .writer(addBoardDto.getWriter())
                .regDate(addBoardDto.getRegDate())
                .boardId((long) addBoardDto.getBoardId())
                .ts(addBoardDto.getTs())
                .domainChannel(domainChannel)
                .build();

        return boardRepository.save(board);
    }

    @Async
    public void sendReply(String ts, String channel, String text, String user, List<Map<String, Object>> files) {
        RestTemplate restTemplate = new RestTemplate();
        BoardEntity board = boardRepository.findByTsAndDomainChannel_Channel(ts, channel)
                .orElseThrow(() -> new RuntimeException("URL을 찾을 수 없습니다."));
        String replyUrl = board.getDomainChannel().getDomain().getReplyUrl();
        Long boardId = board.getBoardId();
        String accountId = board.getDomainChannel().getDomain().getAccountId();
        String accountPw = board.getDomainChannel().getDomain().getAccountPw();

        String paramUserId = board.getDomainChannel().getDomain().getParamNameUserId();
        String paramUserPw = board.getDomainChannel().getDomain().getParamNameUserPw();
        String paramBoardId = board.getDomainChannel().getDomain().getParamNameBoardId();
        String paramBoardContent = board.getDomainChannel().getDomain().getParamNameContent();
        String paramBoardRegUserName = board.getDomainChannel().getDomain().getParamNameRegUsrNm();
        String paramBoardRegDttm = board.getDomainChannel().getDomain().getParamNameRegDttm();

        String loginUrl = "http://localhost/login.act.json";
        MultiValueMap<String, String> loginParams = new LinkedMultiValueMap<>();
        loginParams.add(paramUserId, accountId);
        loginParams.add(paramUserPw, textEncryptor.decrypt(accountPw));

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(loginUrl, loginParams, String.class);
        String sessionCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(paramBoardId, boardId);
        body.add(paramBoardContent, text);
        body.add(paramBoardRegUserName, user);
        body.add(paramBoardRegDttm, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        if (files != null && !files.isEmpty()) {
            for (Map<String, Object> fileInfo : files) {
                String downloadUrl = (String) fileInfo.get("url_private_download");
                String fileName = (String) fileInfo.get("name");

                if (downloadUrl != null) {
                    Resource fileResource = downloadSlackFile(downloadUrl, fileName);
                    HttpHeaders fileHeaders = new HttpHeaders();
                    fileHeaders.setContentDispositionFormData("file", fileName);
                    fileHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    HttpEntity<Resource> fileEntity = new HttpEntity<>(fileResource, fileHeaders);

                    body.add("file", fileEntity);
                }
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        if (sessionCookie != null) {
            headers.add(HttpHeaders.COOKIE, sessionCookie.split(";")[0]);
        }
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(replyUrl, requestEntity, String.class);
        } catch (RestClientException e) {
            log.error("전송 실패: {}", e.getMessage());
        }
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
}
