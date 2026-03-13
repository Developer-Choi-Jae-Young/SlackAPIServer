package co.acta.slackwebhook.service;

import co.acta.slackwebhook.entity.DomainEntity;
import co.acta.slackwebhook.repository.DomainRepository;
import co.acta.slackwebhook.service.modal.interfaces.SlackModalAPI;
import co.acta.slackwebhook.utils.CallRestAPI;
import co.acta.slackwebhook.utils.LoginType;
import co.acta.slackwebhook.utils.SlackMessageFrame;
import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.entity.BoardEntity;
import co.acta.slackwebhook.entity.DomainChannelEntity;
import co.acta.slackwebhook.repository.BoardRepository;
import co.acta.slackwebhook.repository.DomainChannelRepository;
import co.acta.slackwebhook.service.message.interfaces.SlackMessageAPI;
import co.acta.slackwebhook.utils.SlackModalFrame;
import co.acta.slackwebhook.vo.DomainInfo;
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
    public void sendAPI(AddBoardDto boardDto, String domain, List<MultipartFile> files) {
        String parentTs = boardDto.getParentBoardId() != null ? boardRepository.findByBoardId((long) boardDto.getParentBoardId()).map(BoardEntity::getTs).orElse(null) : null;
        List<DomainChannelEntity> domainChannelList = domainChannelRepository.findByDomain_Domain(domain);

        domainChannelList.forEach((data) -> {
            boardDto.setLink(data.getDomain().getViewUrl());
            String ts = callRestAPI.sendMessage(boardDto, data.getChannel(), parentTs, () -> {
                List<Map<String, Object>> blocks = new ArrayList<>();

                for (SlackMessageFrame frame : SlackMessageFrame.values()) {
                    findMessageBean(frame.getServiceClass()).ifPresent(service -> {
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

    private Optional<SlackMessageAPI> findMessageBean(Class<? extends SlackMessageAPI> clazz) {
        return slackMessageAPIList.stream().filter(clazz::isInstance).findFirst();
    }

    @Transactional
    public void addDomainChannel(String domain, String viewUrl, String loginUrl, String replyUrl, String channel, String replyIdValue, String replyPwValue, String paramUserId, String paramUserPw,String paramBoardId, String paramContent, String paramRegUser, String paramRegDttm) {
        String encPassword = textEncryptor.encrypt(replyPwValue);
        DomainChannelEntity domainChannelEntity = domainChannelRepository.findByChannel(channel).orElse(null);

        if(domainChannelEntity == null) {
            DomainEntity domainEntity = DomainEntity.builder()
                    .domain(domain)
                    .viewUrl(viewUrl)
                    .loginUrl(loginUrl)
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
            domainChannelRepository.save(domainChannel);
        } else {
            DomainEntity domainEntity = domainChannelEntity.getDomain();
            domainEntity.setDomain(domain);
            domainEntity.setViewUrl(viewUrl);
            domainEntity.setLoginUrl(loginUrl);
            domainEntity.setReplyUrl(replyUrl);
            domainEntity.setAccountId(replyIdValue);
            domainEntity.setAccountPw(encPassword);
            domainEntity.setParamNameUserId(paramUserId);
            domainEntity.setParamNameUserPw(paramUserPw);
            domainEntity.setParamNameBoardId(paramBoardId);
            domainEntity.setParamNameContent(paramContent);
            domainEntity.setParamNameRegUsrNm(paramRegUser);
            domainEntity.setParamNameRegDttm(paramRegDttm);
        }
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
        BoardEntity board = boardRepository.findByTsAndDomainChannel_Channel(ts, channel)
                .orElseThrow(() -> new RuntimeException("URL을 찾을 수 없습니다."));

        String replyUrl = board.getDomainChannel().getDomain().getReplyUrl();
        String loginUrl = board.getDomainChannel().getDomain().getLoginUrl();
        String accountId = board.getDomainChannel().getDomain().getAccountId();
        String accountPw = board.getDomainChannel().getDomain().getAccountPw();
        String paramUserId = board.getDomainChannel().getDomain().getParamNameUserId();
        String paramUserPw = board.getDomainChannel().getDomain().getParamNameUserPw();
        String paramBoardId = board.getDomainChannel().getDomain().getParamNameBoardId();
        String paramBoardContent = board.getDomainChannel().getDomain().getParamNameContent();
        String paramBoardRegUserName = board.getDomainChannel().getDomain().getParamNameRegUsrNm();
        String paramBoardRegDttm = board.getDomainChannel().getDomain().getParamNameRegDttm();
        Long boardId = board.getBoardId();

        HttpHeaders httpHeaders = callRestAPI.login(loginUrl, paramUserId, paramUserPw, accountId, textEncryptor.decrypt(accountPw), LoginType.Session);
        callRestAPI.reply(replyUrl, paramBoardId, paramBoardContent, paramBoardRegUserName, paramBoardRegDttm, boardId, text, user, httpHeaders, files);
    }

    public void openModal(String triggerId, String channelId) {
        DomainChannelEntity domainChannelEntity = domainChannelRepository.findByChannel(channelId).orElse(null);
        DomainEntity domainEntity = domainChannelEntity == null ? null : domainChannelEntity.getDomain();
        if(domainEntity != null) domainEntity.setAccountPw(textEncryptor.decrypt(domainEntity.getAccountPw()));
        DomainInfo domainInfo = DomainInfo.of(domainEntity);

        callRestAPI.openModal(triggerId, channelId, () -> {
            List<Map<String, Object>> blocks = new ArrayList<>();

            for (SlackModalFrame frame : SlackModalFrame.values()) {
                findModalBean(frame.getServiceClass()).ifPresent(service -> {
                    Map<String, Object> result = (Map<String, Object>) service.makeModalFrame(domainInfo);
                    if (result != null && !result.isEmpty()) blocks.add(result);
                });
            }

            return blocks;
        });
    }

    private Optional<SlackModalAPI> findModalBean(Class<? extends SlackModalAPI> clazz) {
        return slackModalAPIList.stream().filter(clazz::isInstance).findFirst();
    }
}
