package co.acta.slackwebhook.utils.auth.interfaces;

import co.acta.slackwebhook.utils.LoginType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public interface Authenticate {
    HttpHeaders login(ResponseEntity<String> response);
    boolean supports(LoginType loginType);
}
