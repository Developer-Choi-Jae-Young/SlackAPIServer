package co.acta.slackwebhook.service;

import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.service.interfaces.SlackSendAPI;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Order(4)
@Component
public class SlackMessageContentHeader implements SlackSendAPI {
    @Override
    public Map<?, ?> makeMessageFrame(AddBoardDto boardDto, List<MultipartFile> files, String channelId) {
        Map<String, Object> sectionContentHeader = new HashMap<>();

        sectionContentHeader.put("type", "header");
        Map<String, String> subjectHeaderElement = new HashMap<>();
        subjectHeaderElement.put("type", "plain_text");
        subjectHeaderElement.put("text", "📝 상세 내용");
        sectionContentHeader.put("text", subjectHeaderElement);

        return sectionContentHeader;
    }
}
