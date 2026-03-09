package co.acta.slackwebhook.service;

import co.acta.slackwebhook.entity.DomainEntity;
import co.acta.slackwebhook.repository.DomainRepository;
import co.acta.slackwebhook.utils.CallRestAPI;
import co.acta.slackwebhook.utils.SlackMessageFrame;
import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.entity.BoardEntity;
import co.acta.slackwebhook.entity.DomainChannelEntity;
import co.acta.slackwebhook.repository.BoardRepository;
import co.acta.slackwebhook.repository.DomainChannelRepository;
import co.acta.slackwebhook.service.interfaces.SlackSendAPI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final List<SlackSendAPI> slackSendAPIList;
    private final CallRestAPI callRestAPI;

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
            BoardEntity board = addBoard(boardDto);
        });
    }

    private Optional<SlackSendAPI> findBean(Class<? extends SlackSendAPI> clazz) {
        return slackSendAPIList.stream().filter(clazz::isInstance).findFirst();
    }

    @Transactional
    public void addDomainChannel(String domain, String viewUrl, String replyUrl, String channel) {
        DomainEntity duplicateEntity = domainRepository.findByDomain(domain).orElse(null);

        if(duplicateEntity != null){
            throw new RuntimeException("도메인이 이미 존재합니다.");
        }

        DomainEntity domainEntity = DomainEntity.builder()
                .domain(domain)
                .viewUrl(viewUrl)
                .replyUrl(replyUrl)
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
    public BoardEntity addBoard(AddBoardDto addBoardDto) {
        BoardEntity board = BoardEntity.builder()
                .title(addBoardDto.getTitle())
                .content(addBoardDto.getContent())
                .writer(addBoardDto.getWriter())
                .regDate(addBoardDto.getRegDate())
                .boardId((long) addBoardDto.getBoardId())
                .ts(addBoardDto.getTs())
                .build();

        return boardRepository.save(board);
    }

    public void sendReply(String ts, String channel, String text, String user) {

    }
}
