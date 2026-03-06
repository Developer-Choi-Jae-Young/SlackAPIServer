package co.acta.slackwebhook.Utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class HttpHeader {
    private static final String slackToken = "";
    public static HttpHeaders headers = new HttpHeaders();

    static {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(slackToken);
    }
}
