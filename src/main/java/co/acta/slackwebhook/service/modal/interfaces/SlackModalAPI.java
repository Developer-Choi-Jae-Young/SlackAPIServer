package co.acta.slackwebhook.service.modal.interfaces;

import co.acta.slackwebhook.dto.request.AddBoardDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface SlackModalAPI {
    Map<?, ?> makeModalFrame();
}
