package co.acta.slackwebhook.service.message;

import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.service.message.interfaces.SlackMessageAPI;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SlackMessageContent implements SlackMessageAPI {
    @Override
    public Map<?, ?> makeMessageFrame(AddBoardDto boardDto, List<MultipartFile> files, String channelId) {
        Map<String, Object> sectionContentBlock = new HashMap<>();

        sectionContentBlock.put("type", "section");
        Map<String, Object> contentText = new HashMap<>();
        Map<String, String> contentElement = new HashMap<>();
        contentElement.put("type", "plain_text");
        contentElement.put("text", boardDto.getContent());
        contentText.put("text", contentElement);
        sectionContentBlock.put("text", contentElement);

        return sectionContentBlock;
    }
}
