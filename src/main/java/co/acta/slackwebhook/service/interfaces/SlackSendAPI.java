package co.acta.slackwebhook.service.interfaces;

import co.acta.slackwebhook.dto.request.AddBoardDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface SlackSendAPI {
    Map<?, ?> makeMessageFrame(AddBoardDto boardDto, List<MultipartFile> files, String channelId);
}
