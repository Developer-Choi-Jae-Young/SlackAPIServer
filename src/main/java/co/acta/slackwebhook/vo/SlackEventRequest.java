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
        private String subtype;
        private String ts;
        private String text;
        private String user;
        @JsonProperty("thread_ts")
        private String threadTs;
        @JsonProperty("deleted_ts")
        private String deletedTs;
        private String channel;
        @JsonProperty("bot_id")
        private String botId;
        private List<SlackFile> files;
        private EditedMessage message;
        @JsonProperty("previous_message")
        private EditedMessage previousMessage;

        public boolean isUserReplyMessage() {
            return "message".equals(type)
                    && !"message_changed".equals(subtype)
                    && !"message_deleted".equals(subtype)
                    && botId == null
                    && threadTs != null
                    && !threadTs.isEmpty()
                    && !threadTs.equals(ts);
        }

        public boolean isUserReplyEdited() {
            return "message".equals(type)
                    && "message_changed".equals(subtype)
                    && message != null
                    && message.getBotId() == null
                    && message.getThreadTs() != null
                    && !message.getThreadTs().equals(message.getTs());
        }

        public boolean isUserReplyDeleted() {
            return "message".equals(type)
                    && "message_deleted".equals(subtype)
                    && deletedTs != null
                    && previousMessage != null
                    && previousMessage.getThreadTs() != null
                    && !previousMessage.getThreadTs().equals(deletedTs);
        }
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EditedMessage {
        private String text;
        private String user;
        @JsonProperty("thread_ts")
        private String threadTs;
        @JsonProperty("bot_id")
        private String botId;
        private String ts;
        private List<SlackFile> files;
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
