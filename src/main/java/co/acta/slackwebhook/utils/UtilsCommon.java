package co.acta.slackwebhook.utils;

import co.acta.slackwebhook.service.message.interfaces.SlackMessageAPI;
import co.acta.slackwebhook.service.modal.interfaces.SlackModalAPI;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class UtilsCommon {
    public static String getHost(HttpServletRequest request) {
        String rawOrigin = request.getHeader("Origin");
        if (rawOrigin == null || rawOrigin.isEmpty()) {
            rawOrigin = request.getHeader("Referer");
        }
        if (rawOrigin == null || rawOrigin.isEmpty()) {
            rawOrigin = request.getHeader("Host");
        }
        if (rawOrigin == null || rawOrigin.isEmpty()) {
            return "";
        }

        String finalHost = "";
        try {
            if (rawOrigin.startsWith("http")) {
                java.net.URL url = new URL(rawOrigin);
                finalHost = url.getHost();
                if (url.getPort() != -1) {
                    finalHost += ":" + url.getPort();
                }
            } else {
                finalHost = rawOrigin;
            }
        } catch (Exception e) {
            finalHost = rawOrigin.replace("http://", "").replace("https://", "").split("/")[0];
        }

        return finalHost;
    }

    public static Optional<SlackModalAPI> findModalBean(List<SlackModalAPI> slackModalAPIList, Class<? extends SlackModalAPI> clazz) {
        return slackModalAPIList.stream().filter(clazz::isInstance).findFirst();
    }

    public static Optional<SlackMessageAPI> findMessageBean(List<SlackMessageAPI> slackMessageAPIList, Class<? extends SlackMessageAPI> clazz) {
        return slackMessageAPIList.stream().filter(clazz::isInstance).findFirst();
    }
}
