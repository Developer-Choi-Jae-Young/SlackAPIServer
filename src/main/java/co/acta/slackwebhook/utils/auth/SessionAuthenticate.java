package co.acta.slackwebhook.utils.auth;

import co.acta.slackwebhook.utils.enums.LoginType;
import co.acta.slackwebhook.utils.auth.interfaces.Authenticate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class SessionAuthenticate implements Authenticate {
    @Override
    public HttpHeaders login(ResponseEntity<String> response) {
        String sessionCookies = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        if (sessionCookies != null) headers.add(HttpHeaders.COOKIE, sessionCookies.split(";")[0]);
        return headers;
    }

    @Override
    public boolean supports(LoginType loginType) {
        return loginType == LoginType.Session;
    }
}
