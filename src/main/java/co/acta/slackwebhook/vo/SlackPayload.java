package co.acta.slackwebhook.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackPayload {
    private String type;
    private ViewContext view;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ViewContext {
        private String private_metadata;
        private ViewState state;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ViewState {
        private Map<String, Map<String, ActionValue>> values;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActionValue {
        private String value;
    }

    public String getActionValue(String blockId, String actionId) {
        try {
            return view.getState().getValues().get(blockId).get(actionId).getValue();
        } catch (Exception e) {
            return null;
        }
    }
}
