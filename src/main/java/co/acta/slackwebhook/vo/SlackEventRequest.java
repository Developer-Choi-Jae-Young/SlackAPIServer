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
        private String subtype;   // message_changed, message_deleted
        private String ts;        // 이 메시지 자체의 ts
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
        private EditedMessage message; // message_changed 이벤트 시 수정된 메시지 정보
        @JsonProperty("previous_message")
        private EditedMessage previousMessage; // message_deleted 이벤트 시 삭제된 메시지 정보

        public boolean isUserReplyMessage() {
            return "message".equals(type)
                    && subtype == null
                    && botId == null
                    && threadTs != null
                    && !threadTs.isEmpty()
                    && !threadTs.equals(ts); // 스레드 원글(ts == threadTs)은 제외, 순수 답글만
        }

        public boolean isUserReplyEdited() {
            return "message".equals(type)
                    && "message_changed".equals(subtype)
                    && message != null
                    && message.getBotId() == null
                    && message.getThreadTs() != null
                    && !message.getThreadTs().equals(message.getTs()); // 스레드 원글 수정 제외, 순수 답글만
        }

        public boolean isUserReplyDeleted() {
            return "message".equals(type)
                    && "message_deleted".equals(subtype)
                    && deletedTs != null
                    && previousMessage != null
                    && previousMessage.getThreadTs() != null
                    && !previousMessage.getThreadTs().equals(deletedTs); // 원글 삭제 제외, 순수 답글만
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
