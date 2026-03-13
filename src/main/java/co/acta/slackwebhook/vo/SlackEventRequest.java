package co.acta.slackwebhook.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackEventRequest {
    private String challenge;
    private EventDetail event;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventDetail {
        private String type;
        private String text;
        private String user;
        @JsonProperty("thread_ts")
        private String threadTs;
        private String channel;
        @JsonProperty("bot_id")
        private String botId;
        private List<SlackFile> files;

        public boolean isUserReplyMessage() {
            return "message".equals(type)
                    && botId == null
                    && threadTs != null
                    && !threadTs.isEmpty();
        }
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SlackFile {
        private String id;
        private String name;
        @JsonProperty("url_private")
        private String urlPrivate;
        @JsonProperty("url_private_download")
        private String urlPrivateDownload;
    }
}
