package co.acta.slackwebhook.utils;

import java.util.HashMap;
import java.util.Map;

public class UtilsModal {
    protected Map<String, Object> plainText(String text) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "plain_text");
        map.put("text", text);
        return map;
    }
}
