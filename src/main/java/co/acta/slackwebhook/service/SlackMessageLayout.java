package co.acta.slackwebhook.service;

import co.acta.slackwebhook.dto.request.AddBoardDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SlackMessageLayout {
    public Map<String, Object> makeLayout(AddBoardDto boardDto, List<Map<String, Object>> blocks, String channelId) {
        Map<String, Object> body = new HashMap<>();

        body.put("channel", channelId);
        body.put("blocks", blocks);
        body.put("text", "새 게시글이 등록되었습니다: " + boardDto.getTitle());

        return body;
    }
}
