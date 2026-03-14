package co.acta.slackwebhook.service.message;

import co.acta.slackwebhook.dto.request.AddBoardDto;
import co.acta.slackwebhook.service.message.interfaces.SlackMessageAPI;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 두 번째 구분선 블록 — SlackMessageDivider와 동일한 역할이지만
 * findMessageBean()이 class 타입으로 찾기 때문에 별도 클래스로 분리
 */
@Component
public class SlackMessageDivider2 implements SlackMessageAPI {
    @Override
    public Map<?, ?> makeMessageFrame(AddBoardDto boardDto, List<MultipartFile> files, String channelId) {
        Map<String, Object> divider = new HashMap<>();
        divider.put("type", "divider");
        return divider;
    }
}
