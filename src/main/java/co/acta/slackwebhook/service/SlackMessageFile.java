package co.acta.slackwebhook.service;

import co.acta.slackwebhook.Utils.CallRestAPI;
import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.service.interfaces.SlackSendAPI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@Order(6)
@Component
@RequiredArgsConstructor
public class SlackMessageFile implements SlackSendAPI {
    private final CallRestAPI callRestAPI;

    @Override
    public Map<?, ?> makeMessageFrame(AddBoardDto boardDto, List<MultipartFile> files, String channelId) {
        Map<String, Object> fileLinkBlock = new HashMap<>();

        if (files != null && !files.isEmpty()) {
            List<Map<String, String>> uploadedFiles = new ArrayList<>();

            for (MultipartFile mFile : files) {
                if (mFile.isEmpty()) continue;
                uploadedFiles.add(callRestAPI.filesGetUploadURLExternal(mFile));
            }

            if (!uploadedFiles.isEmpty()) {
                callRestAPI.filesCompleteUploadExternal(uploadedFiles, channelId).stream().forEach(fInfo -> {
                    String permalink = (String) fInfo.get("permalink");
                    String name = (String) fInfo.get("name");

                    fileLinkBlock.put("type", "section");
                    Map<String, String> linkText = new HashMap<>();
                    linkText.put("type", "mrkdwn");
                    linkText.put("text", String.format("📎 *첨부파일:* <%s|%s>", permalink, name));
                    fileLinkBlock.put("text", linkText);
                });
            }
        }

        return fileLinkBlock;
    }
}
