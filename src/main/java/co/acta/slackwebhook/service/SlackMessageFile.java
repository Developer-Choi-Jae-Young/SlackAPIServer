package co.acta.slackwebhook.service;

import co.acta.slackwebhook.utils.CallRestAPI;
import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.service.interfaces.SlackSendAPI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlackMessageFile implements SlackSendAPI {
    private final CallRestAPI callRestAPI;

    @Override
    public Map<?, ?> makeMessageFrame(AddBoardDto boardDto, List<MultipartFile> files, String channelId) {
        Map<String, Object> fileLinkBlock = new HashMap<>();
        List<Map<String, String>> fields = new ArrayList<>();

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

                    Map<String, String> field = new HashMap<>();
                    field.put("type", "mrkdwn");
                    field.put("text", String.format("📎 *첨부파일:* <%s|%s>", permalink, name));
                    fields.add(field);
                });
                fileLinkBlock.put("type", "section");
                fileLinkBlock.put("fields", fields);
            }
        }

        return fileLinkBlock;
    }
}
