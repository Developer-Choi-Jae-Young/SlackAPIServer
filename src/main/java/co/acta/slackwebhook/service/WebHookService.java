package co.acta.slackwebhook.service;

import co.acta.slackwebhook.Utils.MetaData;
import co.acta.slackwebhook.dto.request.AddWebHookDTO;
import co.acta.slackwebhook.dto.response.ResUserInfoDto;
import co.acta.slackwebhook.entity.MemberEntity;
import co.acta.slackwebhook.repository.MemberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebHookService {
    private final RestTemplate restTemplate;
    private final MemberRepository memberRepository;
    private final String SLACK_TOKEN = "";

    public void addMember(AddWebHookDTO dto) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
        httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + SLACK_TOKEN);
        String url = "https://slack.com/api/users.info?user=" + dto.getUserToken();

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
            memberRepository.findByUserTokenAndDelYn(dto.getUserToken(), false).orElseGet(() -> memberRepository.save(memberEntity));
        }
    }

    public List<?> getListAll() {
        return MetaData.webHook;
    }
}
