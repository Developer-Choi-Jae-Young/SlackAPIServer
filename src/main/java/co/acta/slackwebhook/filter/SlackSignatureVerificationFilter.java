package co.acta.slackwebhook.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Slack으로부터 오는 요청의 서명을 검증하는 필터.
 *
 * Slack은 모든 요청에 아래 두 헤더를 포함합니다:
 *   - X-Slack-Request-Timestamp : 요청 시각 (Unix timestamp)
 *   - X-Slack-Signature         : HMAC-SHA256 서명 (v0=xxxx 형식)
 *
 * 검증 절차:
 *   1. 타임스탬프가 5분 이상 오래됐으면 Replay Attack으로 판단하여 거부
 *   2. "v0:{timestamp}:{body}" 문자열을 Signing Secret으로 HMAC-SHA256 해싱
 *   3. 계산된 서명과 헤더의 서명이 일치하면 통과, 아니면 401 반환
 */
@Slf4j
@Component
public class SlackSignatureVerificationFilter extends OncePerRequestFilter {

    private static final String SLACK_SIGNATURE_HEADER = "X-Slack-Signature";
    private static final String SLACK_TIMESTAMP_HEADER = "X-Slack-Request-Timestamp";
    private static final long REPLAY_ATTACK_THRESHOLD_SECONDS = 60 * 5L;

    @Value("${slack.signing-secret}")
    private String slackSigningSecret;

    @PostConstruct
    public void validateSigningSecret() {
        if (!StringUtils.hasText(slackSigningSecret)) {
            throw new IllegalStateException(
                "[SlackSignatureFilter] slack.signing-secret이 설정되지 않았습니다. " +
                "환경변수 SLACK_SIGNING_SECRET을 반드시 설정하세요."
            );
        }
        log.info("[SlackSignatureFilter] Slack 서명 검증 필터가 활성화되었습니다.");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String slackSignature = request.getHeader(SLACK_SIGNATURE_HEADER);
        String slackTimestamp = request.getHeader(SLACK_TIMESTAMP_HEADER);

        if (slackSignature == null || slackTimestamp == null) {
            filterChain.doFilter(request, response);
            return;
        }

        long requestTimestamp = Long.parseLong(slackTimestamp);
        long currentTimestamp = System.currentTimeMillis() / 1000;
        if (Math.abs(currentTimestamp - requestTimestamp) > REPLAY_ATTACK_THRESHOLD_SECONDS) {
            log.warn("[SlackSignatureFilter] Replay Attack 의심 요청 차단. timestamp={}", requestTimestamp);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 요청 시각입니다.");
            return;
        }

        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        String requestBody = new String(cachedRequest.getCachedBody(), StandardCharsets.UTF_8);

        String baseString = "v0:" + slackTimestamp + ":" + requestBody;
        String computedSignature = "v0=" + computeHmacSha256(slackSigningSecret, baseString);

        if (!safeEquals(computedSignature, slackSignature)) {
            log.warn("[SlackSignatureFilter] 서명 검증 실패. expected={}, actual={}", computedSignature, slackSignature);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 Slack 요청입니다.");
            return;
        }

        filterChain.doFilter(cachedRequest, response);
    }

    private String computeHmacSha256(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 계산 실패", e);
        }
    }

    private boolean safeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // Slack 이벤트/인터랙션만 서명 검증, add-board는 BO 시스템 호출이므로 제외
        return !uri.equals("/slack/event")
                && !uri.equals("/slack/interactivity")
                && !uri.equals("/slack/add-domain-channel");
    }
}
