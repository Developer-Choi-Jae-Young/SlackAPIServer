package co.acta.slackwebhook.service.message;

import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.service.message.interfaces.SlackMessageAPI;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Component
public class SlackMessageInfo implements SlackMessageAPI {
    @Override
    public Map<?, ?> makeMessageFrame(AddBoardDto boardDto, List<MultipartFile> files, String channelId) {
        Map<String, Object> sectionBlock = new HashMap<>();

        sectionBlock.put("type", "section");
        List<Map<String, String>> fields = new ArrayList<>();
        Map<String, String> writerText = new HashMap<>();
        writerText.put("type", "mrkdwn");
        writerText.put("text", String.format("*작성자: %s*", boardDto.getWriter() != null ? boardDto.getWriter() : ""));
        fields.add(writerText);
        Map<String, String> regText = new HashMap<>();
        regText.put("type", "mrkdwn");
        regText.put("text", String.format("*작성일: %s*", boardDto.getRegDate() != null ? boardDto.getRegDate() : ""));
        fields.add(regText);
        sectionBlock.put("fields", fields);

        return sectionBlock;
    }
}
