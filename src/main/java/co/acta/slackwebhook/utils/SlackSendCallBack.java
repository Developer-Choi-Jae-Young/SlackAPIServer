package co.acta.slackwebhook.utils;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface SlackSendCallBack {
    List<Map<String, Object>> callback();
}
