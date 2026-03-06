package co.acta.slackwebhook.service;

import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.service.interfaces.SlackSendAPI;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Order(7)
@Component
public class SlackMessageLink implements SlackSendAPI {
    @Override
    public Map<?, ?> makeMessageFrame(AddBoardDto boardDto, List<MultipartFile> files, String channelId) {
        Map<String, Object> actionBlock = new HashMap<>();

        if(boardDto.getLink().contains("https://") || boardDto.getLink().contains("http://")) {
            actionBlock.put("type", "actions");
            Map<String, Object> buttonElement = new HashMap<>();
            buttonElement.put("type", "button");
            Map<String, String> buttonText = new HashMap<>();
            buttonText.put("type", "plain_text");
            buttonText.put("text", "게시글 보기");
            buttonElement.put("text", buttonText);
            buttonElement.put("url", boardDto.getLink());
            buttonElement.put("style", "primary");
            List<Map<String, Object>> elements = new ArrayList<>();
            elements.add(buttonElement);
            actionBlock.put("elements", elements);
        }

        return actionBlock;
    }
}
