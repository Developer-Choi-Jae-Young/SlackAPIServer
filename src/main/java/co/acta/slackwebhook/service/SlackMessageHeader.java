package co.acta.slackwebhook.service;

import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.service.interfaces.SlackSendAPI;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SlackMessageHeader implements SlackSendAPI {
    @Override
    public Map<?, ?> makeMessageFrame(AddBoardDto boardDto, List<MultipartFile> files, String channelId) {
        Map<String, Object> headerBlock = new HashMap<>();

        headerBlock.put("type", "header");
        Map<String, String> headerText = new HashMap<>();
        headerText.put("type", "plain_text");
        headerText.put("text", String.format("🔔 제목 : %s", boardDto.getTitle()));
        headerBlock.put("text", headerText);

        return headerBlock;
    }
}
