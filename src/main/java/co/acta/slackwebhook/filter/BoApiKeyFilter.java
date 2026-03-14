package co.acta.slackwebhook.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * BO → 연계서버 요청 인증 필터
 * /bo/** 경로에 대해 X-BO-Api-Key 헤더 검증
 * application.properties에 bo.api-key 설정 필요
 */
@Slf4j
@Component
public class BoApiKeyFilter extends OncePerRequestFilter {

    private static final String BO_API_KEY_HEADER = "X-BO-Api-Key";

    @Value("${bo.api-key}")
    private String boApiKey;

    @PostConstruct
    public void validateApiKey() {
        if (!StringUtils.hasText(boApiKey)) {
            throw new IllegalStateException(
                "[BoApiKeyFilter] bo.api-key가 설정되지 않았습니다. " +
                "환경변수 BO_API_KEY를 반드시 설정하세요."
            );
        }
        log.info("[BoApiKeyFilter] BO API Key 인증 필터가 활성화되었습니다.");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestApiKey = request.getHeader(BO_API_KEY_HEADER);

        if (!StringUtils.hasText(requestApiKey) || !requestApiKey.equals(boApiKey)) {
            log.warn("[BoApiKeyFilter] 인증 실패. uri={}, apiKey={}", request.getRequestURI(), requestApiKey);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 API Key입니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/bo/");
    }
}
