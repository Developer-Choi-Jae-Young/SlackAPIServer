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
    public DomainInfo addDomainChannel(DomainChannelRequest request, String channel) {
        String encPassword = textEncryptor.encrypt(request.getReplyPw());
        DomainChannelEntity domainChannelEntity = domainChannelRepository.findByChannel(channel).orElse(null);

        DomainEntity destDomain = null;
        if(domainChannelEntity == null) {
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
        } else {
            destDomain = domainChannelEntity.getDomain();
            destDomain.setDomain(request.getHost());
            destDomain.setViewUrl(request.getView());
            destDomain.setLoginUrl(request.getLogin());
            destDomain.setReplyUrl(request.getReply());
            destDomain.setAccountId(request.getReplyId());
            destDomain.setAccountPw(encPassword);
            destDomain.setParamNameUserId(request.getParamUserId());
            destDomain.setParamNameUserPw(request.getParamUserPw());
            destDomain.setParamNameBoardId(request.getParamBoardId());
            destDomain.setParamNameContent(request.getParamContent());
            destDomain.setParamNameRegUsrNm(request.getParamRegUser());
            destDomain.setParamNameRegDttm(request.getParamRegDttm());
        }

        return DomainInfo.of(destDomain);
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
    public ResponseEntity<String> sendReply(String ts, String channel, String text, String user, List<SlackEventRequest.SlackFile> files) throws CustomException {
        BoardEntity board = boardRepository.findByTsAndDomainChannel_Channel(ts, channel)
                .orElseThrow(() -> new CustomException(ExceptionInfo.BASIC_INFO_NOT_FOUNT));

        BoardDomainInfo info = BoardDomainInfo.of(board);
        String decryptedPw = textEncryptor.decrypt(info.getAccountPw());

        HttpHeaders httpHeaders = callRestAPI.login(info.getLoginUrl(),
                info.getParamUserId(),
                info.getParamUserPw(),
                info.getAccountId(),
                decryptedPw,
                LoginType.Session);

        return callRestAPI.reply(info, text, user, httpHeaders, files);
    }

    public ResponseEntity<Map> openModal(String triggerId, String channelId) throws CustomException {
        DomainChannelEntity domainChannelEntity = domainChannelRepository.findByChannel(channelId).orElse(null);
        DomainEntity domainEntity = domainChannelEntity == null ? null : domainChannelEntity.getDomain();
        if(domainEntity != null) domainEntity.setAccountPw(textEncryptor.decrypt(domainEntity.getAccountPw()));
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
