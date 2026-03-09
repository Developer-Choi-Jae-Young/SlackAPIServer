package co.acta.slackwebhook.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class HttpHeader {
    private static final String slackToken = "";
    public static final HttpHeaders headers = new HttpHeaders();

    static {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(slackToken);
    }
}
