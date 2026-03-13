package co.acta.slackwebhook.service;

import co.acta.slackwebhook.entity.DomainEntity;
import co.acta.slackwebhook.exception.CustomException;
import co.acta.slackwebhook.exception.ExceptionInfo;
import co.acta.slackwebhook.repository.DomainRepository;
import co.acta.slackwebhook.service.modal.interfaces.SlackModalAPI;
import co.acta.slackwebhook.utils.CallRestAPI;
import co.acta.slackwebhook.utils.UtilsCommon;
import co.acta.slackwebhook.utils.enums.LoginType;
import co.acta.slackwebhook.utils.enums.SlackMessageFrame;
import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.entity.BoardEntity;
import co.acta.slackwebhook.entity.DomainChannelEntity;
import co.acta.slackwebhook.repository.BoardRepository;
import co.acta.slackwebhook.repository.DomainChannelRepository;
import co.acta.slackwebhook.service.message.interfaces.SlackMessageAPI;
import co.acta.slackwebhook.utils.enums.SlackModalFrame;
import co.acta.slackwebhook.vo.BoardDomainInfo;
import co.acta.slackwebhook.vo.DomainChannelRequest;
import co.acta.slackwebhook.vo.DomainInfo;
import co.acta.slackwebhook.vo.SlackEventRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebHookService {
    private final DomainChannelRepository domainChannelRepository;
    private final DomainRepository domainRepository;
    private final BoardRepository boardRepository;
    private final List<SlackMessageAPI> slackMessageAPIList;
    private final List<SlackModalAPI> slackModalAPIList;
    private final CallRestAPI callRestAPI;
    private final TextEncryptor textEncryptor;

    @Transactional
    public List<BoardDomainInfo> sendAPI(AddBoardDto boardDto, String domain, List<MultipartFile> files) {
        String parentTs = boardDto.getParentBoardId() != null ? boardRepository.findByBoardId((long) boardDto.getParentBoardId()).map(BoardEntity::getTs).orElse(null) : null;
        List<DomainChannelEntity> domainChannelList = domainChannelRepository.findByDomain_Domain(domain);
        List<BoardDomainInfo> boardDomainInfoList = new ArrayList<>();

        domainChannelList.forEach((data) -> {
            boardDto.setLink(data.getDomain().getViewUrl());
            String ts = callRestAPI.sendMessage(boardDto, data.getChannel(), parentTs, () -> {
                List<Map<String, Object>> blocks = new ArrayList<>();

                for (SlackMessageFrame frame : SlackMessageFrame.values()) {
                    UtilsCommon.findMessageBean(slackMessageAPIList, frame.getServiceClass()).ifPresent(service -> {
                        Map<String, Object> result = (Map<String, Object>) service.makeMessageFrame(boardDto, files, data.getChannel());
                        if (result != null && !result.isEmpty()) blocks.add(result);
                    });
                }

                return blocks;
            });

            boardDto.setTs(ts);
            boardDomainInfoList.add(BoardDomainInfo.of(addBoard(boardDto, data)));
        });

        return boardDomainInfoList;
    }

    @Transactional
    public DomainInfo addDomainChannel(DomainChannelRequest request, String channel) throws CustomException {
        String encPassword = textEncryptor.encrypt(request.getReplyPw());
        DomainChannelEntity domainChannelEntity = domainChannelRepository.findByChannel(channel).orElse(null);

        try {
            DomainEntity destDomain;
            if (domainChannelEntity == null) {
                DomainEntity domainEntity = DomainEntity.builder()
                        .domain(request.getHost()).viewUrl(request.getView()).loginUrl(request.getLogin())
                        .replyUrl(request.getReply()).accountId(request.getReplyId()).accountPw(encPassword)
                        .paramNameUserId(request.getParamUserId()).paramNameUserPw(request.getParamUserPw())
                        .paramNameBoardId(request.getParamBoardId()).paramNameContent(request.getParamContent())
                        .paramNameRegUsrNm(request.getParamRegUser()).paramNameRegDttm(request.getParamRegDttm()).build();

                DomainChannelEntity domainChannel = DomainChannelEntity.builder()
                        .domain(domainEntity)
                        .channel(channel)
                        .build();

                destDomain = domainRepository.save(domainEntity);
                domainChannelRepository.save(domainChannel);
                log.info("[addDomainChannel] 신규 도메인 채널 등록 완료. channel={}, domain={}", channel, request.getHost());
            } else {
                destDomain = domainChannelEntity.getDomain();
                destDomain.update(request, encPassword);
                log.info("[addDomainChannel] 도메인 채널 정보 업데이트 완료. channel={}, domain={}", channel, request.getHost());
            }

            return DomainInfo.of(destDomain);

        } catch (Exception e) {
            log.error("[addDomainChannel] 도메인 채널 저장 실패. channel={}, error={}", channel, e.getMessage(), e);
            throw new CustomException(ExceptionInfo.DOMAIN_CHANNEL_SAVE_ERROR);
        }
    }

    @Transactional
    public BoardEntity addBoard(AddBoardDto addBoardDto, DomainChannelEntity domainChannel) {
        BoardEntity board = BoardEntity.builder()
                .title(addBoardDto.getTitle()).content(addBoardDto.getContent())
                .writer(addBoardDto.getWriter()).regDate(addBoardDto.getRegDate())
                .boardId((long) addBoardDto.getBoardId())
                .ts(addBoardDto.getTs()).domainChannel(domainChannel).build();

        return boardRepository.save(board);
    }

    @Async
    public void sendReply(String ts, String channel, String text, String user, List<SlackEventRequest.SlackFile> files) {
        try {
            BoardEntity board = boardRepository.findByTsAndDomainChannel_Channel(ts, channel)
                    .orElseThrow(() -> new CustomException(ExceptionInfo.BASIC_INFO_NOT_FOUND));

            BoardDomainInfo info = BoardDomainInfo.of(board);
            String decryptedPw = textEncryptor.decrypt(info.getAccountPw());

            HttpHeaders httpHeaders = callRestAPI.login(info.getLoginUrl(),
                    info.getParamUserId(),
                    info.getParamUserPw(),
                    info.getAccountId(),
                    decryptedPw,
                    LoginType.Session);

            callRestAPI.reply(info, text, user, httpHeaders, files);
        } catch (CustomException e) {
            log.error("[sendReply] 처리 실패: code={}, message={}", e.getExceptionInfo().getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[sendReply] 예상치 못한 오류 발생: {}", e.getMessage(), e);
        }
    }

    public ResponseEntity<Map<String, Object>> openModal(String triggerId, String channelId) throws CustomException {
        DomainChannelEntity domainChannelEntity = domainChannelRepository.findByChannel(channelId).orElse(null);
        DomainEntity domainEntity = domainChannelEntity == null ? null : domainChannelEntity.getDomain();
        if(domainEntity != null) domainEntity.maskDecryptedPassword(textEncryptor.decrypt(domainEntity.getAccountPw()));
        DomainInfo domainInfo = DomainInfo.of(domainEntity);

        return callRestAPI.openModal(triggerId, channelId, () -> {
            List<Map<String, Object>> blocks = new ArrayList<>();

            for (SlackModalFrame frame : SlackModalFrame.values()) {
                UtilsCommon.findModalBean(slackModalAPIList, frame.getServiceClass()).ifPresent(service -> {
                    Map<String, Object> result = (Map<String, Object>) service.makeModalFrame(domainInfo);
                    if (result != null && !result.isEmpty()) blocks.add(result);
                });
            }

            return blocks;
        });
    }
}
