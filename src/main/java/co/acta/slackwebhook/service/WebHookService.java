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
import co.acta.slackwebhook.entity.ReplyEntity;
import co.acta.slackwebhook.repository.BoardRepository;
import co.acta.slackwebhook.repository.ReplyRepository;
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
    private final ReplyRepository replyRepository;
    private final List<SlackMessageAPI> slackMessageAPIList;
    private final List<SlackModalAPI> slackModalAPIList;
    private final CallRestAPI callRestAPI;
    private final TextEncryptor textEncryptor;

    @Transactional
    public List<BoardDomainInfo> sendAPI(AddBoardDto boardDto, String domain, List<MultipartFile> files) {
        String parentTs = boardDto.getParentBoardId() != null
                ? boardRepository.findByBoardId((long) boardDto.getParentBoardId()).map(BoardEntity::getTs).orElse(null)
                : null;

        List<DomainChannelEntity> domainChannelList = domainChannelRepository.findByDomain_Domain(domain);
        List<BoardDomainInfo> boardDomainInfoList = new ArrayList<>();

        for (DomainChannelEntity data : domainChannelList) {
            // boardDto는 공유 객체이므로 채널별로 필요한 값만 로컬로 처리
            boardDto.setLink(data.getDomain().getViewUrl());

            String ts;
            try {
                ts = callRestAPI.sendMessage(boardDto, data.getChannel(), parentTs, () -> {
                    List<Map<String, Object>> blocks = new ArrayList<>();
                    for (SlackMessageFrame frame : SlackMessageFrame.values()) {
                        UtilsCommon.findMessageBean(slackMessageAPIList, frame.getServiceClass()).ifPresent(service -> {
                            Map<String, Object> result = (Map<String, Object>) service.makeMessageFrame(boardDto, files, data.getChannel());
                            if (result != null && !result.isEmpty()) blocks.add(result);
                        });
                    }
                    return blocks;
                });
            } catch (CustomException e) {
                log.error("[sendAPI] 채널={} 메시지 전송 실패, DB 저장 건너뜀. code={}, message={}",
                        data.getChannel(), e.getExceptionInfo().getErrorCode(), e.getMessage());
                continue;
            }

            // ts는 채널별로 독립적으로 저장 — boardDto.setTs 대신 addBoard에 직접 전달
            AddBoardDto boardDtoForSave = AddBoardDto.builder()
                    .parentBoardId(boardDto.getParentBoardId())
                    .boardId(boardDto.getBoardId())
                    .title(boardDto.getTitle())
                    .content(boardDto.getContent())
                    .writer(boardDto.getWriter())
                    .regDate(boardDto.getRegDate())
                    .link(boardDto.getLink())
                    .ts(ts)
                    .build();
            boardDomainInfoList.add(BoardDomainInfo.of(addBoard(boardDtoForSave, data)));
        }

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
                        .replyUrl(request.getReply()).replyUpdateUrl(request.getReplyUpdate()).replyDeleteUrl(request.getReplyDelete())
                        .accountId(request.getReplyId()).accountPw(encPassword)
                        .paramNameUserId(request.getParamUserId()).paramNameUserPw(request.getParamUserPw())
                        .paramNameBoardId(request.getParamBoardId()).paramNameReplyId(request.getParamReplyId())
                        .paramNameReplyIdKey(request.getParamReplyIdKey()).paramNameContent(request.getParamContent())
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
    @Transactional
    public void sendReply(String ts, String replyTs, String channel, String text, String user, List<SlackEventRequest.SlackFile> files) {
        try {
            BoardEntity board = boardRepository.findByTsAndDomainChannel_Channel(ts, channel)
                    .orElseThrow(() -> new CustomException(ExceptionInfo.BASIC_INFO_NOT_FOUND));

            BoardDomainInfo info = BoardDomainInfo.of(board);
            String decryptedPw = textEncryptor.decrypt(info.getAccountPw());

            HttpHeaders httpHeaders = callRestAPI.login(
                    info.getLoginUrl(), info.getParamUserId(), info.getParamUserPw(),
                    info.getAccountId(), decryptedPw, LoginType.Session);

            // BO 시스템에 댓글 등록, 응답에서 BO 댓글 PK 파싱 (없으면 null)
            String boReplyId = callRestAPI.reply(info, text, user, httpHeaders, files);

            // 등록 성공 시 Slack reply ts + boReplyId를 DB에 저장
            replyRepository.save(ReplyEntity.builder()
                    .ts(ts).replyTs(replyTs).boReplyId(boReplyId).board(board).build());
            log.info("[sendReply] 답글 DB 저장 완료. ts={}, replyTs={}, boReplyId={}, channel={}", ts, replyTs, boReplyId, channel);

        } catch (CustomException e) {
            log.error("[sendReply] 처리 실패: code={}, message={}", e.getExceptionInfo().getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[sendReply] 예상치 못한 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * Slack에서 답글 수정 이벤트 수신 시 → BO 시스템에 수정 반영
     * Slack은 이미 수정된 상태이므로 Slack API 재호출 불필요
     */
    @Async
    @Transactional
    public void updateReply(String channel, String originalTs, String newText, String user, List<SlackEventRequest.SlackFile> files) {
        try {
            ReplyEntity reply = replyRepository.findByReplyTs(originalTs)
                    .orElseThrow(() -> new CustomException(ExceptionInfo.REPLY_NOT_FOUND));

            BoardDomainInfo info = BoardDomainInfo.of(reply.getBoard());
            String decryptedPw = textEncryptor.decrypt(info.getAccountPw());

            HttpHeaders httpHeaders = callRestAPI.login(
                    info.getLoginUrl(), info.getParamUserId(), info.getParamUserPw(),
                    info.getAccountId(), decryptedPw, LoginType.Session);

            // BO 댓글 PK 없으면 수정 불가 — BO가 등록 응답에 PK를 포함하지 않은 경우
            if (reply.getBoReplyId() == null) {
                throw new CustomException(ExceptionInfo.REPLY_BO_ID_NOT_FOUND);
            }

            // BO 시스템 댓글 수정 (boReplyId가 있으면 포함해서 전송)
            callRestAPI.updateBoReply(info, reply.getBoReplyId(), newText, user, httpHeaders, files);
            log.info("[updateReply] BO 답글 수정 완료. replyTs={}, boReplyId={}, channel={}", originalTs, reply.getBoReplyId(), channel);

        } catch (CustomException e) {
            log.error("[updateReply] 처리 실패: code={}, message={}", e.getExceptionInfo().getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[updateReply] 예상치 못한 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * Slack에서 답글 삭제 이벤트 수신 시 → BO 시스템에 삭제 반영 + DB 삭제
     * Slack은 이미 삭제된 상태이므로 Slack API 재호출 불필요
     */
    @Async
    @Transactional
    public void deleteReply(String channel, String deletedTs) {
        try {
            ReplyEntity reply = replyRepository.findByReplyTs(deletedTs)
                    .orElseThrow(() -> new CustomException(ExceptionInfo.REPLY_NOT_FOUND));

            BoardDomainInfo info = BoardDomainInfo.of(reply.getBoard());
            String decryptedPw = textEncryptor.decrypt(info.getAccountPw());

            HttpHeaders httpHeaders = callRestAPI.login(
                    info.getLoginUrl(), info.getParamUserId(), info.getParamUserPw(),
                    info.getAccountId(), decryptedPw, LoginType.Session);

            // BO 댓글 PK 없으면 삭제 불가 — BO가 등록 응답에 PK를 포함하지 않은 경우
            if (reply.getBoReplyId() == null) {
                throw new CustomException(ExceptionInfo.REPLY_BO_ID_NOT_FOUND);
            }

            // BO 시스템에 삭제 반영 (boReplyId가 있으면 포함해서 전송)
            callRestAPI.deleteBoReply(info, reply.getBoReplyId(), httpHeaders);

            // DB에서도 답글 삭제
            replyRepository.delete(reply);
            log.info("[deleteReply] 답글 삭제 완료. replyTs={}, boReplyId={}, channel={}", deletedTs, reply.getBoReplyId(), channel);

        } catch (CustomException e) {
            log.error("[deleteReply] 처리 실패: code={}, message={}", e.getExceptionInfo().getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[deleteReply] 예상치 못한 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * BO에서 답글 수정 시 → Slack 스레드 메시지도 수정
     */
    @Async
    @Transactional(readOnly = true)
    public void boUpdateReplyToSlack(String boReplyId, String newContent) {
        try {
            ReplyEntity reply = replyRepository.findByBoReplyId(boReplyId)
                    .orElseThrow(() -> new CustomException(ExceptionInfo.REPLY_NOT_FOUND));

            String channel = reply.getBoard().getDomainChannel().getChannel();
            callRestAPI.updateMessage(channel, reply.getReplyTs(), newContent);
            log.info("[boUpdateReplyToSlack] Slack 답글 수정 완료. boReplyId={}, replyTs={}", boReplyId, reply.getReplyTs());

        } catch (CustomException e) {
            log.error("[boUpdateReplyToSlack] 처리 실패: code={}, message={}", e.getExceptionInfo().getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[boUpdateReplyToSlack] 예상치 못한 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * BO에서 답글 삭제 시 → Slack 스레드 메시지도 삭제
     */
    @Async
    @Transactional
    public void boDeleteReplyToSlack(String boReplyId) {
        try {
            ReplyEntity reply = replyRepository.findByBoReplyId(boReplyId)
                    .orElseThrow(() -> new CustomException(ExceptionInfo.REPLY_NOT_FOUND));

            String channel = reply.getBoard().getDomainChannel().getChannel();
            callRestAPI.deleteMessage(channel, reply.getReplyTs());

            replyRepository.delete(reply);
            log.info("[boDeleteReplyToSlack] Slack 답글 삭제 완료. boReplyId={}, replyTs={}", boReplyId, reply.getReplyTs());

        } catch (CustomException e) {
            log.error("[boDeleteReplyToSlack] 처리 실패: code={}, message={}", e.getExceptionInfo().getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[boDeleteReplyToSlack] 예상치 못한 오류 발생: {}", e.getMessage(), e);
        }
    }

    public ResponseEntity<Map<String, Object>> openModal(String triggerId, String channelId) throws CustomException {
        DomainChannelEntity domainChannelEntity = domainChannelRepository.findByChannel(channelId).orElse(null);
        DomainEntity domainEntity = domainChannelEntity == null ? null : domainChannelEntity.getDomain();
        if (domainEntity != null) domainEntity.maskDecryptedPassword(textEncryptor.decrypt(domainEntity.getAccountPw()));
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
