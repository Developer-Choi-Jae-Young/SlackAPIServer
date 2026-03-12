package co.acta.slackwebhook.service.message;

import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.service.message.interfaces.SlackMessageAPI;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SlackMessageHeader implements SlackMessageAPI {
    @Override
    public Map<?, ?> makeMessageFrame(AddBoardDto boardDto, List<MultipartFile> files, String channelId) {
        Map<String, Object> headerBlock = new HashMap<>();

        if(boardDto.getTitle() != null && !boardDto.getTitle().isEmpty()) {
            headerBlock.put("type", "header");
            Map<String, String> headerText = new HashMap<>();
            headerText.put("type", "plain_text");
            headerText.put("text", String.format("🔔 제목 : %s", boardDto.getTitle()));
            headerBlock.put("text", headerText);
        }

        return headerBlock;
    }
}
