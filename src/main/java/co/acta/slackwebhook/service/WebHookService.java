package co.acta.slackwebhook.service;

import co.acta.slackwebhook.Utils.MetaData;
import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.dto.request.AddWebHookDTO;
import co.acta.slackwebhook.dto.response.ResUserInfoDto;
import co.acta.slackwebhook.entity.BoardEntity;
import co.acta.slackwebhook.entity.DomainChannelEntity;
import co.acta.slackwebhook.entity.MemberEntity;
import co.acta.slackwebhook.repository.BoardRepository;
import co.acta.slackwebhook.repository.DomainChannelRepository;
import co.acta.slackwebhook.repository.MemberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebHookService {
    private final RestTemplate restTemplate;
    private final MemberRepository memberRepository;
    private final DomainChannelRepository domainChannelRepository;
    private final BoardRepository boardRepository;

    private final String slackToken = "";

    public void addToken(AddWebHookDTO dto) {

    }

    public void addMember(AddWebHookDTO dto) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
        httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + slackToken);
        String url = "https://slack.com/api/users.info?user=" + dto.getUser_id();

        ResponseEntity<ResUserInfoDto> returnData =
                restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(httpHeaders), ResUserInfoDto.class);

        ResUserInfoDto bodyData = returnData.getBody();
        boolean isOk = bodyData.isOk();

        if(isOk) {
            MemberEntity memberEntity = MemberEntity.builder()
                    .name(bodyData.getUser().getName())
                    .userToken(bodyData.getUser().getId())
                    .email(bodyData.getUser().getProfile().getEmail())
                    .phone(bodyData.getUser().getProfile().getPhone())
                    .build();
            memberRepository.findByUserTokenAndDelYn(dto.getUser_id(), false).orElseGet(() -> memberRepository.save(memberEntity));
        }
    }
    public void sendAPI(String message, String domain) {
        String url = "https://slack.com/api/chat.postMessage";

        List<DomainChannelEntity> domainChannelList = domainChannelRepository.findByDomain(domain);
        domainChannelList.forEach((data) -> {
            Map<String, String> payload = new HashMap<>();
            payload.put("channel", data.getChannel());
            payload.put("text", message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(slackToken);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> res = restTemplate.postForEntity(url, request, String.class);
        });
    }

    @Transactional
    public void addDomainChannel(String domain, String channel) {
        DomainChannelEntity domainChannel = DomainChannelEntity.builder()
                .domain(domain)
                .channel(channel)
                .build();

        domainChannelRepository.save(domainChannel);
    }

    @Transactional
    public void addBoard(AddBoardDto addBoardDto) {
        BoardEntity board = BoardEntity.builder()
                .title(addBoardDto.getTitle())
                .content(addBoardDto.getContent())
                .writer(addBoardDto.getWriter())
                .regDate(addBoardDto.getRegDate())
                .link(addBoardDto.getLink())
                .build();

        boardRepository.save(board);
    }
}
